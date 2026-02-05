package com.dangbun.domain.cleaning.refactor.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.cleaning.refactor.adapter.in.web.dto.response.GetCleaningDetailListResponse;
import com.dangbun.domain.cleaning.refactor.adapter.in.web.dto.response.GetCleaningListResponse;
import com.dangbun.domain.cleaning.refactor.adapter.in.web.dto.response.GetCleaningUnassignedResponse;
import com.dangbun.domain.cleaning.refactor.adapter.out.CleaningJpaEntity;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.duty.refactor.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.global.context.DutyContext;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.dangbun.domain.cleaning.refactor.application.port.in.query.CleaningQuery;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CleaningQueryService implements CleaningQuery {

    private final DutyQueryPort dutyQueryPort;
    private final MemberCleaningRepository memberCleaningRepository;
    private final CleaningRepository cleaningRepository;

    @Override
    public List<GetCleaningListResponse> getCleaningList(List<Long> memberIds) {
        List<Duty> duties = (memberIds == null || memberIds.isEmpty())
                ? dutyQueryPort.findAll()
                : dutyQueryPort.findDistinctDutiesByMemberIds(memberIds);

        return duties.stream()
                .map(GetCleaningListResponse::of)
                .toList();
    }

    @Override
    public List<GetCleaningDetailListResponse> getCleaningDetailList(List<Long> memberIds) {

        DutyJpaEntity duty = DutyContext.get();

        List<CleaningJpaEntity> cleaningJpaEntities = (memberIds == null || memberIds.isEmpty())
                ? cleaningRepository.findAllByDuty(duty)
                : cleaningRepository.findByDutyIdAndMemberIdsWithMembersJoin(duty.getDutyId(), memberIds);

        return cleaningJpaEntities.stream()
                .map(cleaning -> {
                    List<String> names = memberCleaningRepository.findMembersByCleaningId(cleaning.getCleaningId())
                            .stream().map(MemberJpaEntity::getName).toList();

                    List<String> displayed = names.stream().limit(2).toList();
                    return GetCleaningDetailListResponse.of(cleaning.getName(), displayed, names.size());
                })
                .toList();
    }

    @Override
    public List<GetCleaningUnassignedResponse> getUnassignedCleanings() {
        Long placeId = MemberContext.get().getPlace().getPlaceId();

        List<CleaningJpaEntity> cleaningJpaEntities = cleaningRepository.findUnassignedCleaningsByPlaceId(placeId);

        return cleaningJpaEntities.stream()
                .map(cleaning -> GetCleaningUnassignedResponse.of(cleaning.getCleaningId(), cleaning.getName()))
                .toList();
    }
}
