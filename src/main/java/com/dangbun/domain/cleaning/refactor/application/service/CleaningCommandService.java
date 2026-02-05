package com.dangbun.domain.cleaning.refactor.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.refactor.ChecklistCommandPort;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.refactor.adapter.in.web.dto.request.PostCleaningCreateRequest;
import com.dangbun.domain.cleaning.refactor.adapter.in.web.dto.request.PutCleaningUpdateRequest;
import com.dangbun.domain.cleaning.refactor.adapter.in.web.dto.response.PostCleaningResponse;
import com.dangbun.domain.cleaning.refactor.adapter.out.CleaningJpaEntity;
import com.dangbun.domain.cleaning.exception.custom.*;
import com.dangbun.domain.cleaning.refactor.application.port.out.CleaningCommandPort;
import com.dangbun.domain.cleaning.refactor.application.port.out.CleaningQueryPort;
import com.dangbun.domain.cleaning.refactor.domain.Cleaning;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import com.dangbun.domain.cleaningImage.repository.CleaningImageRepository;
import com.dangbun.domain.cleaningdate.refactor.application.port.out.CleaningDateCommandPort;
import com.dangbun.domain.cleaningdate.refactor.domain.CleaningDate;
import com.dangbun.domain.cleaningdate.repository.CleaningDateRepository;
import com.dangbun.domain.duty.refactor.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.member.original.repository.MemberRepository;
import com.dangbun.domain.member.refactor.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.refactor.domain.Member;
import com.dangbun.domain.membercleaning.refactor.MemberCleaningCommandPort;
import com.dangbun.domain.membercleaning.refactor.domain.MemberCleaning;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.place.original.entity.Place;
import com.dangbun.global.context.MemberContext;
import com.dangbun.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import static com.dangbun.domain.cleaning.refactor.domain.CleaningRepeatType.*;
import static com.dangbun.domain.cleaning.response.status.CleaningExceptionResponse.*;

import com.dangbun.domain.cleaning.refactor.application.port.in.command.CleaningCommandUseCase;

@UseCase
@RequiredArgsConstructor
@Transactional
public class CleaningCommandService implements CleaningCommandUseCase {

    private final DutyQueryPort dutyQueryPort;
    private final CleaningQueryPort cleaningQueryPort;
    private final CleaningCommandPort cleaningCommandPort;
    private final MemberQueryPort memberQueryPort;
    private final CleaningDateCommandPort cleaningDateCommandPort;

    private final MemberCleaningRepository memberCleaningRepository;
    private final CleaningRepository cleaningRepository;
    private final MemberRepository memberRepository;
    private final CleaningDateRepository cleaningDateRepository;
    private final ChecklistRepository checklistRepository;
    private final CleaningImageRepository cleaningImageRepository;
    private final S3Service s3Service;
    private final MemberCleaningCommandPort memberCleaningCommandPort;
    private final ChecklistCommandPort checklistCommandPort;

    @Override
    public PostCleaningResponse createCleaning(PostCleaningCreateRequest request) {
        Place place = MemberContext.get().getPlace();

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
        Place place = MemberContext.get().getPlace();

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

        memberCleaningRepository.deleteAllByCleaningJpaEntity_CleaningId(cleaningId);
        List<Member> newMembers = memberQueryPort.findAllByNameIn(request.members());
        List<MemberCleaning> newMemberCleanings = newMembers.stream()
                .map(m -> MemberCleaning.of(m.getMemberId(),cleaningId))
                .toList();
        memberCleaningCommandPort.saveAll(newMemberCleanings);

        cleaningDateRepository.deleteAllByCleaningJpaEntity_CleaningId(cleaningId);

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
        CleaningJpaEntity cleaningJpaEntity = cleaningRepository.findById(cleaningId)
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        List<Checklist> checklists = checklistRepository.findByCleaningJpaEntity_CleaningId(cleaningJpaEntity.getCleaningId());
        for (Checklist checklist : checklists) {
            cleaningImageRepository.findByChecklist_ChecklistId(checklist.getChecklistId())
                    .ifPresent(img -> s3Service.deleteFile(img.getS3Key()));
        }

        cleaningRepository.delete(cleaningJpaEntity);
    }
}
