package com.dangbun.domain.cleaning.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.refactor.ChecklistCommandPort;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.adapter.in.web.dto.request.PostCleaningCreateRequest;
import com.dangbun.domain.cleaning.adapter.in.web.dto.request.PutCleaningUpdateRequest;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.PostCleaningResponse;
import com.dangbun.domain.cleaning.application.port.out.CleaningCommandPort;
import com.dangbun.domain.cleaning.application.port.out.CleaningQueryPort;
import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.cleaning.exception.custom.CleaningAlreadyExistsException;
import com.dangbun.domain.cleaning.exception.custom.CleaningNotFoundException;
import com.dangbun.domain.cleaning.exception.custom.DutyNotFoundException;
import com.dangbun.domain.cleaning.exception.custom.InvalidDateFormatException;
import com.dangbun.domain.cleaningImage.repository.CleaningImageRepository;
import com.dangbun.domain.cleaningdate.application.port.out.CleaningDateCommandPort;
import com.dangbun.domain.cleaningdate.domain.CleaningDate;
import com.dangbun.domain.duty.refactor.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.member.refactor.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.refactor.domain.Member;
import com.dangbun.domain.membercleaning.application.port.out.MemberCleaningCommandPort;
import com.dangbun.domain.membercleaning.domain.MemberCleaning;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.global.context.MemberContext;
import com.dangbun.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import static com.dangbun.domain.cleaning.domain.CleaningRepeatType.*;
import static com.dangbun.domain.cleaning.exception.status.CleaningExceptionResponse.*;

import com.dangbun.domain.cleaning.application.port.in.command.CleaningCommandUseCase;

@UseCase
@RequiredArgsConstructor
@Transactional
public class CleaningCommandService implements CleaningCommandUseCase {

    private final DutyQueryPort dutyQueryPort;
    private final CleaningQueryPort cleaningQueryPort;
    private final CleaningCommandPort cleaningCommandPort;
    private final MemberQueryPort memberQueryPort;
    private final CleaningDateCommandPort cleaningDateCommandPort;
    private final MemberCleaningCommandPort memberCleaningCommandPort;
    private final ChecklistCommandPort checklistCommandPort;

    /*
     * TODO: Checklist/CleaningImage 도메인 헥사고날 아키텍처 전환 시 수정
     * ChecklistRepository -> ChecklistQueryPort
     * CleaningImageRepository -> CleaningImageQueryPort
     */
    private final ChecklistRepository checklistRepository;
    private final CleaningImageRepository cleaningImageRepository;
    private final S3Service s3Service;

    @Override
    public PostCleaningResponse createCleaning(PostCleaningCreateRequest request) {
        PlaceJpaEntity place = MemberContext.get().getPlace();

        Duty duty = null;
        if (request.dutyId() != null) {
            duty = dutyQueryPort.findById(request.dutyId())
                    .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));
        }

        if (cleaningQueryPort.existsByNameAndDutyIdAndPlaceId(request.cleaningName(), duty.getDutyId().value(), place.getPlaceId())) {
            throw new CleaningAlreadyExistsException(CLEANING_ALREADY_EXISTS);
        }

        String repeatDaysStr = (request.repeatType() == WEEKLY && request.repeatDays() != null)
                ? String.join(",", request.repeatDays())
                : null;

        Cleaning cleaning = Cleaning.withoutId(
                request.cleaningName(),
                request.repeatType(),
                repeatDaysStr,
                request.dutyId(),
                request.needPhoto(),
                place.getPlaceId()
        );
        Cleaning savedCleaning = cleaningCommandPort.save(cleaning);

        if (request.members() != null && !request.members().isEmpty()) {
            List<Member> members = memberQueryPort.findAllByNameIn(request.members());
            List<MemberCleaning> memberCleanings = members.stream()
                    .map(m -> MemberCleaning.of(m.getMemberId(), savedCleaning.getCleaningId().value()))
                    .toList();

            memberCleaningCommandPort.saveAll(memberCleanings);
        }


        List<LocalDate> parsedDates = request.detailDates().stream()
                .map(dateStr -> {
                    try {
                        return LocalDate.parse(dateStr);
                    } catch (DateTimeParseException e) {
                        throw new InvalidDateFormatException(INVALID_DATE_FORMAT);
                    }
                })
                .toList();

        List<CleaningDate> cleaningDates = parsedDates.stream()
                .map(date -> CleaningDate.withoutId(date, savedCleaning.getCleaningId().value()))
                .toList();


        checklistCommandPort.createChecklistByDateAndTime(savedCleaning.getCleaningId().value(), cleaningDates, place.getPlaceId());
        cleaningDateCommandPort.saveAll(cleaningDates);


        return PostCleaningResponse.of(savedCleaning.getCleaningId().value());
    }

    @Override
    public void updateCleaning(Long cleaningId, PutCleaningUpdateRequest request) {
        PlaceJpaEntity place = MemberContext.get().getPlace();

        Cleaning cleaning = cleaningQueryPort.findWithDutyNullableById(cleaningId)
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        Duty duty = null;

        if (request.dutyId() != null) {
            duty = dutyQueryPort.findById(request.dutyId())
                    .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));
        }


        if (cleaningQueryPort.existsByNameAndDutyIdAndCleaningIdNotAndPlaceId(
                request.cleaningName(),
                duty.getDutyId().value(),
                cleaningId,
                place.getPlaceId())) {
            throw new CleaningAlreadyExistsException(CLEANING_ALREADY_EXISTS);
        }

        cleaning.update(request.cleaningName(),
                request.needPhoto(),
                request.repeatType(),
                request.repeatType() == WEEKLY && request.repeatDays() != null
                        ? request.repeatDays().stream()
                        .map(Enum::name).collect(Collectors.joining(","))
                        : null,
                duty.getDutyId().value()
        );


        Cleaning savedCleaning = cleaningCommandPort.save(cleaning);

        memberCleaningCommandPort.deleteAllByCleaningId(cleaningId);
        List<Member> newMembers = memberQueryPort.findAllByNameIn(request.members());
        List<MemberCleaning> newMemberCleanings = newMembers.stream()
                .map(m -> MemberCleaning.of(m.getMemberId(), cleaningId))
                .toList();
        memberCleaningCommandPort.saveAll(newMemberCleanings);

        cleaningDateCommandPort.deleteAllByCleaningId(cleaningId);

        List<LocalDate> parsedDates = request.detailDates().stream()
                .map(dateStr -> {
                    try {
                        return LocalDate.parse(dateStr);
                    } catch (DateTimeParseException e) {
                        throw new InvalidDateFormatException(INVALID_DATE_FORMAT);
                    }
                })
                .toList();

        List<CleaningDate> cleaningDates = parsedDates.stream()
                .map(date -> CleaningDate.withoutId(date, savedCleaning.getCleaningId().value()))
                .toList();

        cleaningDateCommandPort.saveAll(cleaningDates);

    }


    @Override
    public void deleteCleaning(Long cleaningId) {
        Cleaning cleaning = cleaningQueryPort.findById(cleaningId)
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        /*
         * TODO: Checklist/CleaningImage 도메인 헥사고날 아키텍처 전환 시 수정
         * checklistRepository -> ChecklistQueryPort
         * cleaningImageRepository -> CleaningImageQueryPort
         */
        List<Checklist> checklists = checklistRepository.findByCleaningJpaEntity_CleaningId(cleaningId);
        for (Checklist checklist : checklists) {
            cleaningImageRepository.findByChecklist_ChecklistId(checklist.getChecklistId())
                    .ifPresent(img -> s3Service.deleteFile(img.getS3Key()));
        }

        cleaningCommandPort.deleteById(cleaningId);
    }
}
