package com.dangbun.domain.cleaning.service;

import com.dangbun.domain.cleaning.dto.response.GetCleaningListResponse;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CleaningService {

    private final MemberCleaningRepository memberCleaningRepository;
    private final DutyRepository dutyRepository;

    @Transactional()
    public List<GetCleaningListResponse> getCleaningList(List<Long> memberIds) {
        List<Duty> duties = (memberIds == null || memberIds.isEmpty())
                ? dutyRepository.findAll()
                : memberCleaningRepository.findDistinctDutiesByMemberIds(memberIds);

        return duties.stream()
                .map(GetCleaningListResponse::of)
                .toList();
    }
}
