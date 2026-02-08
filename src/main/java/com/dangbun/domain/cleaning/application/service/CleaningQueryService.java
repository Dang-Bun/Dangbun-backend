package com.dangbun.domain.cleaning.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningDetailListResponse;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningListResponse;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningUnassignedResponse;
import com.dangbun.domain.cleaning.application.port.out.CleaningQueryPort;
import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.duty.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.duty.application.port.in.query.GetDutyForCleaningQuery;
import com.dangbun.domain.membercleaning.application.port.in.query.GetMemberCleaningForCleaningQuery;
import com.dangbun.global.context.DutyContext;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.dangbun.domain.cleaning.application.port.in.query.CleaningQuery;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningForDutyQuery;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningsByPlaceQuery;

import java.util.Optional;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CleaningQueryService implements CleaningQuery, GetCleaningsByPlaceQuery, GetCleaningForDutyQuery {

    private final GetDutyForCleaningQuery getDutyForCleaningQuery;
    private final CleaningQueryPort cleaningQueryPort;
    private final GetMemberCleaningForCleaningQuery getMemberCleaningForCleaningQuery;

    @Override
    public List<GetCleaningListResponse> getCleaningList(List<Long> memberIds) {
        List<GetDutyForCleaningQuery.DutyInfo> duties = (memberIds == null || memberIds.isEmpty())
                ? getDutyForCleaningQuery.findAll()
                : getDutyForCleaningQuery.findDistinctDutiesByMemberIds(memberIds);

        return duties.stream()
                .map(GetCleaningListResponse::of)
                .toList();
    }

    @Override
    public List<GetCleaningDetailListResponse> getCleaningDetailList(List<Long> memberIds) {
        /*
         * TODO: DutyContext 헥사고날 아키텍처 전환 시 수정
         * DutyContext.get()이 DutyJpaEntity 대신 도메인 모델이나 dutyId를 반환하도록 변경
         */
        DutyJpaEntity duty = DutyContext.get();
        Long dutyId = duty.getDutyId();

        List<Cleaning> cleanings = (memberIds == null || memberIds.isEmpty())
                ? cleaningQueryPort.findAllByDutyId(dutyId)
                : cleaningQueryPort.findByDutyIdAndMemberIds(dutyId, memberIds);

        return cleanings.stream()
                .map(cleaning -> {
                    List<String> names = getMemberCleaningForCleaningQuery.findMemberNamesByCleaningId(cleaning.getCleaningId().value());

                    List<String> displayed = names.stream().limit(2).toList();
                    return GetCleaningDetailListResponse.of(cleaning.getName(), displayed, names.size());
                })
                .toList();
    }

    @Override
    public List<GetCleaningUnassignedResponse> getUnassignedCleanings() {
        Long placeId = MemberContext.get().getPlace().getPlaceId();

        List<Cleaning> cleanings = cleaningQueryPort.findUnassignedCleaningsByPlaceId(placeId);

        return cleanings.stream()
                .map(cleaning -> GetCleaningUnassignedResponse.of(cleaning.getCleaningId().value(), cleaning.getName()))
                .toList();
    }

    @Override
    public Cleaning getCleaning(Long cleaningId) {
        return cleaningQueryPort.findById(cleaningId)
                .orElseThrow(() -> new IllegalArgumentException("Cleaning not found: " + cleaningId));
    }

    @Override
    public List<Cleaning> getCleaningsByPlaceId(Long placeId) {
        return cleaningQueryPort.findByPlaceId(placeId);
    }

    // GetCleaningForDutyQuery 구현
    @Override
    public List<CleaningInfo> findAllByDutyId(Long dutyId) {
        return cleaningQueryPort.findAllByDutyId(dutyId).stream()
                .map(c -> new CleaningInfo(c.getCleaningId().value(), c.getName(), c.getDutyId()))
                .toList();
    }

    @Override
    public List<CleaningInfo> findAllByIds(List<Long> cleaningIds) {
        return cleaningQueryPort.findAllByIds(cleaningIds).stream()
                .map(c -> new CleaningInfo(c.getCleaningId().value(), c.getName(), c.getDutyId()))
                .toList();
    }

    @Override
    public Optional<CleaningInfo> findById(Long cleaningId) {
        return cleaningQueryPort.findById(cleaningId)
                .map(c -> new CleaningInfo(c.getCleaningId().value(), c.getName(), c.getDutyId()));
    }

    @Override
    public Optional<CleaningInfo> findByCleaningIdAndDutyId(Long cleaningId, Long dutyId) {
        return cleaningQueryPort.findByCleaningIdAndDutyId(cleaningId, dutyId)
                .map(c -> new CleaningInfo(c.getCleaningId().value(), c.getName(), c.getDutyId()));
    }
}
