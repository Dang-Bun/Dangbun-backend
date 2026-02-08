package com.dangbun.domain.memberduty.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.memberduty.application.port.in.command.MemberDutyForDutyUseCase;
import com.dangbun.domain.memberduty.application.port.in.query.GetMemberDutyForDutyQuery;
import com.dangbun.domain.memberduty.application.port.out.MemberDutyCommandPort;
import com.dangbun.domain.memberduty.application.port.out.MemberDutyQueryPort;
import com.dangbun.domain.memberduty.domain.MemberDuty;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional
public class MemberDutyService implements GetMemberDutyForDutyQuery, MemberDutyForDutyUseCase {

    private final MemberDutyQueryPort memberDutyQueryPort;
    private final MemberDutyCommandPort memberDutyCommandPort;

    @Override
    @Transactional(readOnly = true)
    public List<Long> findMemberIdsByDutyId(Long dutyId) {
        return memberDutyQueryPort.findMemberIdsByDutyId(dutyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberDutyMemberInfo> findMemberInfosByDutyId(Long dutyId) {
        return memberDutyQueryPort.findMemberInfosByDutyId(dutyId).stream()
                .map(info -> new MemberDutyMemberInfo(info.memberId(), info.role(), info.name()))
                .toList();
    }

    @Override
    public void saveAllByDutyId(Long dutyId, List<Long> memberIds) {
        List<MemberDuty> memberDuties = memberIds.stream()
                .map(memberId -> MemberDuty.of(memberId, dutyId))
                .toList();

        memberDutyCommandPort.saveAll(memberDuties);
    }

    @Override
    public void deleteAllByDutyId(Long dutyId) {
        memberDutyCommandPort.deleteAllByDutyId(dutyId);
    }
}
