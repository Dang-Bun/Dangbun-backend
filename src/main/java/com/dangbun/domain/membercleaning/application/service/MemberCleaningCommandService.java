package com.dangbun.domain.membercleaning.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.membercleaning.application.port.in.command.MemberCleaningForCleaningUseCase;
import com.dangbun.domain.membercleaning.application.port.in.command.MemberCleaningForDutyUseCase;
import com.dangbun.domain.membercleaning.application.port.out.MemberCleaningCommandPort;
import com.dangbun.domain.membercleaning.domain.MemberCleaning;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional
public class MemberCleaningCommandService implements MemberCleaningForDutyUseCase, MemberCleaningForCleaningUseCase {

    private final MemberCleaningCommandPort memberCleaningCommandPort;

    @Override
    public void deleteAllByCleaningId(Long cleaningId) {
        memberCleaningCommandPort.deleteAllByCleaningId(cleaningId);
    }

    @Override
    public void saveAllByCleaningIdAndMemberIds(Long cleaningId, List<Long> memberIds) {
        List<MemberCleaning> memberCleanings = memberIds.stream()
                .map(memberId -> MemberCleaning.of(memberId, cleaningId))
                .toList();

        memberCleaningCommandPort.saveAll(memberCleanings);
    }
}
