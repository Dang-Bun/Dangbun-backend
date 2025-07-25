package com.dangbun.domain.cleaning.service;

import com.dangbun.domain.cleaning.dto.response.GetCleaningDetailListResponse;
import com.dangbun.domain.cleaning.dto.response.GetCleaningListResponse;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaning.exception.custom.DutyNotFoundException;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dangbun.domain.cleaning.response.status.CleaningExceptionResponse.*;

@RequiredArgsConstructor
@Service
public class CleaningService {

    private final MemberCleaningRepository memberCleaningRepository;
    private final DutyRepository dutyRepository;
    private final CleaningRepository cleaningRepository;

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

}
