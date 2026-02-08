package com.dangbun.domain.duty.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningForDutyQuery;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningForDutyQuery.CleaningInfo;
import com.dangbun.domain.duty.application.port.in.query.*;
import com.dangbun.domain.duty.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.duty.exception.custom.DutyNotFoundException;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.membercleaning.application.port.in.query.GetMemberCleaningForDutyQuery;
import com.dangbun.domain.memberduty.application.port.in.query.GetMemberDutyForDutyQuery;
import com.dangbun.domain.memberduty.application.port.in.query.GetMemberDutyForDutyQuery.MemberDutyMemberInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.dangbun.domain.duty.exception.status.DutyExceptionResponse.DUTY_NOT_FOUND;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DutyQueryService implements DutyQuery, GetDutyForMemberQuery, GetDutyForCleaningQuery {

    private final DutyQueryPort dutyQueryPort;
    private final GetMemberDutyForDutyQuery getMemberDutyForDutyQuery;
    private final GetCleaningForDutyQuery getCleaningForDutyQuery;
    private final GetMemberCleaningForDutyQuery getMemberCleaningForDutyQuery;

    @Override
    public DutyListResult getDutyList(Long placeId) {
        List<Duty> duties = dutyQueryPort.findByPlaceId(placeId);
        return DutyListResult.from(duties);
    }

    @Override
    public DutyMembersResult getDutyMembers(Long dutyId) {
        validateDutyExists(dutyId);

        List<MemberDutyMemberInfo> memberInfos = getMemberDutyForDutyQuery.findMemberInfosByDutyId(dutyId);

        List<DutyMembersResult.MemberItem> members = memberInfos.stream()
                .sorted(
                        Comparator
                                .comparing((MemberDutyMemberInfo m) -> !m.role().equals(MemberRole.MANAGER.name()))
                                .thenComparing(MemberDutyMemberInfo::name, Comparator.nullsLast(String::compareTo))
                )
                .map(m -> new DutyMembersResult.MemberItem(
                        m.memberId(),
                        m.role(),
                        m.name()
                ))
                .toList();

        return DutyMembersResult.of(members);
    }

    @Override
    public DutyCleaningsResult getDutyCleanings(Long dutyId) {
        validateDutyExists(dutyId);

        List<CleaningInfo> cleanings = getCleaningForDutyQuery.findAllByDutyId(dutyId);

        List<DutyCleaningsResult.CleaningItem> items = cleanings.stream()
                .map(c -> new DutyCleaningsResult.CleaningItem(
                        c.cleaningId(),
                        c.name()
                ))
                .toList();

        return DutyCleaningsResult.of(items);
    }

    @Override
    public CleaningInfoListResult getCleaningInfoList(Long dutyId) {
        validateDutyExists(dutyId);

        List<CleaningInfo> cleanings = getCleaningForDutyQuery.findAllByDutyId(dutyId);

        List<CleaningInfoListResult.CleaningInfo> infos = cleanings.stream()
                .map(cleaning -> {
                    Long cleaningId = cleaning.cleaningId();
                    List<String> memberNames = getMemberCleaningForDutyQuery.findMemberNamesByCleaningId(cleaningId);
                    Integer memberCount = getMemberCleaningForDutyQuery.countMembersByCleaningId(cleaningId);

                    List<String> displayedNames = memberNames.stream()
                            .limit(2)
                            .toList();

                    return new CleaningInfoListResult.CleaningInfo(
                            cleaningId,
                            cleaning.name(),
                            displayedNames,
                            memberCount
                    );
                })
                .toList();

        return CleaningInfoListResult.of(infos);
    }

    private void validateDutyExists(Long dutyId) {
        dutyQueryPort.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));
    }

    // GetDutyForMemberQuery 구현
    @Override
    public Optional<GetDutyForMemberQuery.DutyInfo> findByIdAndPlaceId(Long dutyId, Long placeId) {
        return dutyQueryPort.findByIdAndPlaceId(dutyId, placeId)
                .map(duty -> new GetDutyForMemberQuery.DutyInfo(duty.getDutyId().value(), duty.getName()));
    }

    // GetDutyForCleaningQuery 구현
    @Override
    public Optional<GetDutyForCleaningQuery.DutyInfo> findById(Long dutyId) {
        return dutyQueryPort.findById(dutyId)
                .map(duty -> new GetDutyForCleaningQuery.DutyInfo(
                        duty.getDutyId().value(),
                        duty.getName(),
                        duty.getIcon() != null ? duty.getIcon().name() : null
                ));
    }

    @Override
    public List<GetDutyForCleaningQuery.DutyInfo> findAll() {
        return dutyQueryPort.findAll().stream()
                .map(duty -> new GetDutyForCleaningQuery.DutyInfo(
                        duty.getDutyId().value(),
                        duty.getName(),
                        duty.getIcon() != null ? duty.getIcon().name() : null
                ))
                .toList();
    }

    @Override
    public List<GetDutyForCleaningQuery.DutyInfo> findDistinctDutiesByMemberIds(List<Long> memberIds) {
        return dutyQueryPort.findDistinctDutiesByMemberIds(memberIds).stream()
                .map(duty -> new GetDutyForCleaningQuery.DutyInfo(
                        duty.getDutyId().value(),
                        duty.getName(),
                        duty.getIcon() != null ? duty.getIcon().name() : null
                ))
                .toList();
    }
}
