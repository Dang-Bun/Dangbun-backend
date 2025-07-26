package com.dangbun.domain.cleaning.service;

import com.dangbun.domain.cleaning.dto.request.PostCleaningRequest;
import com.dangbun.domain.cleaning.dto.response.*;
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
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dangbun.domain.cleaning.entity.CleaningRepeatType.*;
import static com.dangbun.domain.cleaning.response.status.CleaningExceptionResponse.*;

@RequiredArgsConstructor
@Service
public class CleaningService {

    private final MemberCleaningRepository memberCleaningRepository;
    private final DutyRepository dutyRepository;
    private final CleaningRepository cleaningRepository;
    private final MemberRepository memberRepository;
    private final CleaningDateRepository cleaningDateRepository;


    @Transactional()
    public List<GetCleaningListResponse> getCleaningList(List<Long> memberIds) {
        List<Duty> duties = (memberIds == null || memberIds.isEmpty())
                ? dutyRepository.findAll()
                : memberCleaningRepository.findDistinctDutiesByMemberIds(memberIds);

        return duties.stream()
                .map(GetCleaningListResponse::of)
                .toList();
    }

    @Transactional()
    public List<GetCleaningDetailListResponse> getCleaningDetailList(Long dutyId, List<Long> memberIds) {

        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        List<Cleaning> cleanings = (memberIds == null || memberIds.isEmpty())
                ? cleaningRepository.findAllByDuty(duty)
                : cleaningRepository.findByDutyIdAndMemberIds(dutyId, memberIds);

        return cleanings.stream()
                .map(cleaning -> {
                    List<Member> members = memberCleaningRepository.findMembersByCleaningId(cleaning.getCleaningId());
                    List<String> names = members.stream()
                            .map(Member::getName)
                            .toList();

                    List<String> displayed = names.stream().limit(2).toList();

                    return GetCleaningDetailListResponse.of(cleaning.getName(), displayed, members.size());
                })
                .toList();
    }

    @Transactional()
    public PostCleaningResponse createCleaning(PostCleaningRequest request) {
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
                .build();

        cleaningRepository.save(cleaning);

        if (request.members() != null && !request.members().isEmpty()) {
            List<Member> members = memberRepository.findAllByNameIn((request.members()));
            for (Member member : members) {
                MemberCleaning mc = MemberCleaning.builder()
                        .member(member)
                        .cleaning(cleaning)
                        .build();
                memberCleaningRepository.save(mc);
            }
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

        cleaningDateRepository.saveAll(cleaningDates);

        return PostCleaningResponse.of(cleaning.getCleaningId());
    }
}