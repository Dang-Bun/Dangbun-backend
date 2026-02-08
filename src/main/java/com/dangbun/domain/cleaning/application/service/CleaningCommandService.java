package com.dangbun.domain.cleaning.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.checklist.application.port.in.command.CreateChecklistByDateAndTimeUseCase;
import com.dangbun.domain.checklist.application.port.in.query.GetChecklistForCleaningQuery;
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
import com.dangbun.domain.cleaningImage.application.port.in.command.CleaningImageCommandUseCase;
import com.dangbun.domain.cleaningdate.application.port.in.command.CleaningDateForCleaningUseCase;
import com.dangbun.domain.cleaningdate.domain.CleaningDate;
import com.dangbun.domain.duty.application.port.in.query.GetDutyForCleaningQuery;
import com.dangbun.domain.member.application.port.in.query.GetMemberForCleaningQuery;
import com.dangbun.domain.membercleaning.application.port.in.command.MemberCleaningForCleaningUseCase;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import static com.dangbun.domain.cleaning.domain.CleaningRepeatType.*;
import static com.dangbun.domain.cleaning.exception.status.CleaningExceptionResponse.*;

import com.dangbun.domain.cleaning.application.port.in.command.CleaningCommandUseCase;
import com.dangbun.domain.cleaning.application.port.in.command.CleaningForDutyUseCase;

@UseCase
@RequiredArgsConstructor
@Transactional
public class CleaningCommandService implements CleaningCommandUseCase, CleaningForDutyUseCase {

    private final GetDutyForCleaningQuery getDutyForCleaningQuery;
    private final CleaningQueryPort cleaningQueryPort;
    private final CleaningCommandPort cleaningCommandPort;
    private final GetMemberForCleaningQuery getMemberForCleaningQuery;
    private final CleaningDateForCleaningUseCase cleaningDateForCleaningUseCase;
    private final MemberCleaningForCleaningUseCase memberCleaningForCleaningUseCase;
    private final CreateChecklistByDateAndTimeUseCase createChecklistByDateAndTimeUseCase;

    private final GetChecklistForCleaningQuery getChecklistForCleaningQuery;
    private final CleaningImageCommandUseCase cleaningImageCommandUseCase;

    @Override
    public PostCleaningResponse createCleaning(PostCleaningCreateRequest request) {
        PlaceJpaEntity place = MemberContext.get().getPlace();

        GetDutyForCleaningQuery.DutyInfo dutyInfo = null;
        if (request.dutyId() != null) {
            dutyInfo = getDutyForCleaningQuery.findById(request.dutyId())
                    .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));
        }

        if (cleaningQueryPort.existsByNameAndDutyIdAndPlaceId(request.cleaningName(), dutyInfo.dutyId(), place.getPlaceId())) {
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
            List<GetMemberForCleaningQuery.MemberInfo> members = getMemberForCleaningQuery.findAllByNameIn(request.members());
            List<Long> memberIds = members.stream()
                    .map(GetMemberForCleaningQuery.MemberInfo::memberId)
                    .toList();

            memberCleaningForCleaningUseCase.saveAllByCleaningIdAndMemberIds(savedCleaning.getCleaningId().value(), memberIds);
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

        createChecklistByDateAndTimeUseCase.createChecklistByDateAndTime(savedCleaning.getCleaningId().value(), cleaningDates, place.getPlaceId());
        cleaningDateForCleaningUseCase.saveAllByCleaningId(savedCleaning.getCleaningId().value(), parsedDates);

        return PostCleaningResponse.of(savedCleaning.getCleaningId().value());
    }

    @Override
    public void updateCleaning(Long cleaningId, PutCleaningUpdateRequest request) {
        PlaceJpaEntity place = MemberContext.get().getPlace();

        Cleaning cleaning = cleaningQueryPort.findWithDutyNullableById(cleaningId)
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        GetDutyForCleaningQuery.DutyInfo dutyInfo = null;

        if (request.dutyId() != null) {
            dutyInfo = getDutyForCleaningQuery.findById(request.dutyId())
                    .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));
        }

        if (cleaningQueryPort.existsByNameAndDutyIdAndCleaningIdNotAndPlaceId(
                request.cleaningName(),
                dutyInfo.dutyId(),
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
                dutyInfo.dutyId()
        );

        cleaningCommandPort.save(cleaning);

        memberCleaningForCleaningUseCase.deleteAllByCleaningId(cleaningId);
        List<GetMemberForCleaningQuery.MemberInfo> newMembers = getMemberForCleaningQuery.findAllByNameIn(request.members());
        List<Long> newMemberIds = newMembers.stream()
                .map(GetMemberForCleaningQuery.MemberInfo::memberId)
                .toList();
        memberCleaningForCleaningUseCase.saveAllByCleaningIdAndMemberIds(cleaningId, newMemberIds);

        cleaningDateForCleaningUseCase.deleteAllByCleaningId(cleaningId);

        List<LocalDate> parsedDates = request.detailDates().stream()
                .map(dateStr -> {
                    try {
                        return LocalDate.parse(dateStr);
                    } catch (DateTimeParseException e) {
                        throw new InvalidDateFormatException(INVALID_DATE_FORMAT);
                    }
                })
                .toList();

        cleaningDateForCleaningUseCase.saveAllByCleaningId(cleaningId, parsedDates);
    }


    @Override
    public void deleteCleaning(Long cleaningId) {
        Cleaning cleaning = cleaningQueryPort.findById(cleaningId)
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        List<Long> checklistIds = getChecklistForCleaningQuery.findChecklistIdsByCleaningId(cleaningId);
        for (Long checklistId : checklistIds) {
            cleaningImageCommandUseCase.deleteS3File(checklistId);
        }

        cleaningCommandPort.deleteById(cleaningId);
    }

    // CleaningForDutyUseCase 구현
    @Override
    public void assignCleaningsToDuty(Long dutyId, List<Long> cleaningIds) {
        List<Cleaning> cleanings = cleaningQueryPort.findAllByIds(cleaningIds);

        for (Cleaning cleaning : cleanings) {
            if (cleaning.getDutyId() == null) {
                cleaning.assignToDuty(dutyId);
            }
        }

        cleaningCommandPort.saveAll(cleanings);
    }

    @Override
    public void removeCleaningFromDuty(Long cleaningId) {
        Cleaning cleaning = cleaningQueryPort.findById(cleaningId)
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        cleaning.removeDuty();
        cleaningCommandPort.save(cleaning);
    }
}
