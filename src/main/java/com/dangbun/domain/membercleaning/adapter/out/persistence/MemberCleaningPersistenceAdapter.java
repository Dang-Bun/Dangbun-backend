package com.dangbun.domain.membercleaning.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningRepository;
import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.member.original.repository.MemberRepository;
import com.dangbun.domain.membercleaning.application.port.out.MemberCleaningCommandPort;
import com.dangbun.domain.membercleaning.application.port.out.MemberCleaningQueryPort;
import com.dangbun.domain.membercleaning.domain.MemberCleaning;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class MemberCleaningPersistenceAdapter implements MemberCleaningQueryPort, MemberCleaningCommandPort {

    private final MemberCleaningRepository memberCleaningRepository;
    private final MemberRepository memberRepository;
    private final CleaningRepository cleaningRepository;

    @Override
    public List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds) {
        List<DutyJpaEntity> dutyJpaEntities = memberCleaningRepository.findDistinctDutiesByMemberIds(memberIds);

        return List.of();
    }

    @Override
    public void saveAll(List<MemberCleaning> memberCleanings) {

        List<MemberCleaningJpaEntity> memberCleaningJpaEntities = new ArrayList<>();

        for (MemberCleaning mc : memberCleanings) {
            MemberJpaEntity m = memberRepository.getReferenceById(mc.getMemberId());
            CleaningJpaEntity c = cleaningRepository.getReferenceById(mc.getCleaningId());

            memberCleaningJpaEntities.add(new MemberCleaningJpaEntity(m, c));
        }
        memberCleaningRepository.saveAll(memberCleaningJpaEntities);
    }

    @Override
    public void deleteAllByCleaningId(Long cleaningId) {
        memberCleaningRepository.deleteAllByCleaningJpaEntity_CleaningId(cleaningId);
    }

    @Override
    public List<String> findMemberNamesByCleaningId(Long cleaningId) {
        return memberCleaningRepository.findMembersByCleaningId(cleaningId).stream()
                .map(MemberJpaEntity::getName)
                .toList();
    }
}
