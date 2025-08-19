package com.dangbun.domain.cleaning.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.checklist.service.CreateChecklistService;
import com.dangbun.domain.cleaning.dto.request.PostCleaningCreateRequest;
import com.dangbun.domain.cleaning.dto.request.PutCleaningUpdateRequest;
import com.dangbun.domain.cleaning.dto.response.GetCleaningDetailListResponse;
import com.dangbun.domain.cleaning.dto.response.GetCleaningListResponse;
import com.dangbun.domain.cleaning.dto.response.GetCleaningUnassignedResponse;
import com.dangbun.domain.cleaning.dto.response.PostCleaningResponse;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaning.exception.custom.*;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import com.dangbun.domain.cleaningImage.repository.CleaningImageRepository;
import com.dangbun.domain.cleaningdate.entity.CleaningDate;
import com.dangbun.domain.cleaningdate.repository.CleaningDateRepository;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.global.context.DutyContext;
import com.dangbun.global.context.MemberContext;
import com.dangbun.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import static com.dangbun.domain.cleaning.entity.CleaningRepeatType.WEEKLY;
import static com.dangbun.domain.cleaning.response.status.CleaningExceptionResponse.*;

@RequiredArgsConstructor
@Service
@Transactional
public class CleaningService {

    private final MemberCleaningRepository memberCleaningRepository;
    private final DutyRepository dutyRepository;
    private final CleaningRepository cleaningRepository;
    private final MemberRepository memberRepository;
    private final CleaningDateRepository cleaningDateRepository;
    private final CreateChecklistService createChecklistService;
    private final ChecklistRepository checklistRepository;
    private final CleaningImageRepository cleaningImageRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public List<GetCleaningListResponse> getCleaningList(List<Long> memberIds) {
        List<Duty> duties = (memberIds == null || memberIds.isEmpty())
                ? dutyRepository.findAll()
                : memberCleaningRepository.findDistinctDutiesByMemberIds(memberIds);

        return duties.stream()
                .map(GetCleaningListResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GetCleaningDetailListResponse> getCleaningDetailList(List<Long> memberIds) {

        Duty duty = DutyContext.get();

        List<Cleaning> cleanings = (memberIds == null || memberIds.isEmpty())
                ? cleaningRepository.findAllByDuty(duty)
                : cleaningRepository.findByDutyIdAndMemberIdsWithMembersJoin(duty.getDutyId(), memberIds);

        return cleanings.stream()
                .map(cleaning -> {
                    List<String> names = memberCleaningRepository.findMembersByCleaningId(cleaning.getCleaningId())
                            .stream().map(Member::getName).toList();

                    List<String> displayed = names.stream().limit(2).toList();
                    return GetCleaningDetailListResponse.of(cleaning.getName(), displayed, names.size());
                })
                .toList();
    }


    public PostCleaningResponse createCleaning(PostCleaningCreateRequest request) {
        Place place = MemberContext.get().getPlace();

        Duty duty = null;
        if (request.dutyName() != null) {
            duty = dutyRepository.findById(request.dutyId())
                    .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));
        }

        if (cleaningRepository.existsByNameAndDutyAndPlace(request.cleaningName(), duty, place)) {
            throw new CleaningAlreadyExistsException(CLEANING_ALREADY_EXISTS);
        }

        Cleaning cleaning = Cleaning.builder()
                .name(request.cleaningName())
                .duty(duty)
                .repeatType(request.repeatType())
                .repeatDays(
                        request.repeatType() == WEEKLY && request.repeatDays() != null
                                ? String.join(",", request.repeatDays())
                                : null
                )
                .needPhoto(request.needPhoto())
                .place(place)
                .build();

        cleaningRepository.save(cleaning);

        if (request.members() != null && !request.members().isEmpty()) {
            List<Member> members = memberRepository.findAllByNameIn((request.members()));
            List<MemberCleaning> memberCleanings = members.stream()
                    .map(m -> MemberCleaning.builder().member(m).cleaning(cleaning).build())
                    .toList();
            memberCleaningRepository.saveAll(memberCleanings);
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
                .map(date -> CleaningDate.builder()
                        .date(date)
                        .cleaning(cleaning)
                        .build())
                .toList();
        createChecklistService.createChecklistByDateAndTime(cleaning, cleaningDates, place);
        cleaningDateRepository.saveAll(cleaningDates);

        return PostCleaningResponse.of(cleaning.getCleaningId());
    }


    public void updateCleaning(Long cleaningId, PutCleaningUpdateRequest request) {
        Place place = MemberContext.get().getPlace();

        Cleaning cleaning = cleaningRepository.findWithDutyNullableById(cleaningId)
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        Duty duty = null;
        if (request.dutyName() != null) {
            duty = dutyRepository.findByName(request.dutyName())
                    .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));
        }

        if (cleaningRepository.existsByNameAndDutyAndCleaningIdNotAndPlace(request.cleaningName(), duty, cleaningId, place)) {
            throw new CleaningAlreadyExistsException(CLEANING_ALREADY_EXISTS);
        }

        cleaning.updateInfo(
                request.cleaningName(),
                request.needPhoto(),
                request.repeatType(),
                request.repeatType() == WEEKLY && request.repeatDays() != null
                        ? request.repeatDays().stream()
                        .map(Enum::name).collect(Collectors.joining(","))
                        : null,
                duty
        );

        cleaningRepository.save(cleaning);

        memberCleaningRepository.deleteAllByCleaning_CleaningId(cleaningId);

        List<Member> newMembers = memberRepository.findAllByNameIn(request.members());
        List<MemberCleaning> newMemberCleanings = newMembers.stream()
                .map(m -> MemberCleaning.builder().member(m).cleaning(cleaning).build())
                .toList();
        memberCleaningRepository.saveAll(newMemberCleanings);

        cleaningDateRepository.deleteAllByCleaning_CleaningId(cleaningId);

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
                .map(date -> CleaningDate.builder()
                        .date(date)
                        .cleaning(cleaning)
                        .build())
                .toList();

        cleaningDateRepository.saveAll(cleaningDates);

    }


    public void deleteCleaning(Long cleaningId) {
        Cleaning cleaning = cleaningRepository.findById(cleaningId)
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        List<Checklist> checklists = checklistRepository.findByCleaning_CleaningId(cleaning.getCleaningId());
        for (Checklist checklist : checklists) {
            cleaningImageRepository.findByChecklist_ChecklistId(checklist.getChecklistId())
                    .ifPresent(img -> s3Service.deleteFile(img.getS3Key()));
        }

        cleaningRepository.delete(cleaning);
    }

    @Transactional(readOnly = true)
    public List<GetCleaningUnassignedResponse> getUnassignedCleanings() {
        Long placeId = MemberContext.get().getPlace().getPlaceId();

        List<Cleaning> cleanings = cleaningRepository.findUnassignedCleaningsByPlaceId(placeId);

        return cleanings.stream()
                .map(cleaning -> GetCleaningUnassignedResponse.of(cleaning.getCleaningId(), cleaning.getName()))
                .toList();
    }
}