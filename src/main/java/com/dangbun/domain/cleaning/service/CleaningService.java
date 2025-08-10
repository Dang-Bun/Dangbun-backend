package com.dangbun.domain.cleaning.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.checklist.service.ChecklistService;
import com.dangbun.domain.cleaning.dto.request.PostCleaningCreateRequest;
import com.dangbun.domain.cleaning.dto.request.PutCleaningUpdateRequest;
import com.dangbun.domain.cleaning.dto.response.GetCleaningDetailListResponse;
import com.dangbun.domain.cleaning.dto.response.GetCleaningListResponse;
import com.dangbun.domain.cleaning.dto.response.GetCleaningUnassignedResponse;
import com.dangbun.domain.cleaning.dto.response.PostCleaningResponse;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaning.exception.custom.*;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import com.dangbun.domain.cleaningdate.entity.CleaningDate;
import com.dangbun.domain.cleaningdate.repository.CleaningDateRepository;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.repository.PlaceRepository;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import static com.dangbun.domain.cleaning.entity.CleaningRepeatType.WEEKLY;
import static com.dangbun.domain.cleaning.response.status.CleaningExceptionResponse.*;

@RequiredArgsConstructor
@Service
public class CleaningService {

    private final MemberCleaningRepository memberCleaningRepository;
    private final DutyRepository dutyRepository;
    private final CleaningRepository cleaningRepository;
    private final MemberRepository memberRepository;
    private final CleaningDateRepository cleaningDateRepository;
    private final ChecklistService checkListService;
    private final ChecklistRepository checkListRepository;


    public List<GetCleaningListResponse> getCleaningList(List<Long> memberIds) {
        List<Duty> duties = (memberIds == null || memberIds.isEmpty())
                ? dutyRepository.findAll()
                : memberCleaningRepository.findDistinctDutiesByMemberIds(memberIds);

        return duties.stream()
                .map(GetCleaningListResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GetCleaningDetailListResponse> getCleaningDetailList(Long dutyId, List<Long> memberIds) {

        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        List<Cleaning> cleanings = (memberIds == null || memberIds.isEmpty())
                ? cleaningRepository.findAllByDuty(duty)
                : cleaningRepository.findByDutyIdAndMemberIdsWithMembersJoin(dutyId, memberIds);

        return cleanings.stream()
                .map(cleaning -> {
                    List<String> names = cleaning.getMemberCleanings().stream()
                            .map(mc -> mc.getMember().getName())
                            .toList();

                    List<String> displayed = names.stream().limit(2).toList();
                    return GetCleaningDetailListResponse.of(cleaning.getName(), displayed, names.size());
                })
                .toList();
    }


    public PostCleaningResponse createCleaning(PostCleaningCreateRequest request) {
        Place place = MemberContext.get().getPlace();

        Duty duty = null;
        if (request.dutyName() != null) {
            duty = dutyRepository.findByName(request.dutyName())
                    .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));
        }

        if (cleaningRepository.existsByNameAndDuty(request.cleaningName(), duty)) {
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


        if (request.members() != null && !request.members().isEmpty()) {
            List<Member> members = memberRepository.findAllByNameIn((request.members()));
            for (Member member : members) {
                MemberCleaning mc = MemberCleaning.builder()
                        .member(member)
                        .cleaning(cleaning)
                        .build();
                cleaning.getMemberCleanings().add(mc);
            }
        }

        cleaningRepository.save(cleaning);

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

        return PostCleaningResponse.of(cleaning.getCleaningId());
    }


    public void updateCleaning(Long cleaningId, PutCleaningUpdateRequest request) {
        Cleaning cleaning = cleaningRepository.findWithDutyNullableById(cleaningId)
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        Duty duty = null;
        if (request.dutyName() != null) {
            duty = dutyRepository.findByName(request.dutyName())
                    .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));
        }

        if (cleaningRepository.existsByNameAndDutyAndCleaningIdNot(request.cleaningName(), duty, cleaningId)) {
            throw new CleaningAlreadyExistsException(CLEANING_ALREADY_EXISTS);
        }

        cleaning.updateInfo(
                request.cleaningName(),
                request.needPhoto(),
                request.repeatType(),
                request.repeatType() == WEEKLY && request.repeatDays() != null
                        ? String.join(",", request.repeatDays())
                        : null,
                duty
        );
        cleaning.updateMembers(memberRepository.findAllByNameIn(request.members()));
        cleaningRepository.save(cleaning);


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

        cleaningDateRepository.deleteAllByCleaning_CleaningId(cleaningId);

        List<Checklist> checkLists = checkListRepository.findByCleaning_CleaningId(cleaningId);
        for(Checklist checkList : checkLists) {
            checkListService.deleteChecklist(checkList.getChecklistId());
        }

        cleaningRepository.delete(cleaning);
    }

    @Transactional(readOnly = true)
    public List<GetCleaningUnassignedResponse> getUnassignedCleanings() {
        Long placeId = MemberContext.get().getPlace().getPlaceId();

        List<Cleaning> cleanings = cleaningRepository.findUnassignedCleaningsByPlaceId(placeId);

        return cleanings.stream()
                .map(cleaning -> GetCleaningUnassignedResponse.of(cleaning.getName()))
                .toList();
    }
}