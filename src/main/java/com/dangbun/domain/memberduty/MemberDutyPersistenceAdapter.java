package com.dangbun.domain.memberduty;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.member.original.repository.MemberRepository;
import com.dangbun.domain.memberduty.entity.MemberDutyJpaEntity;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class MemberDutyPersistenceAdapter implements MemberDutyCommandPort {

    private final MemberRepository memberRepository;
    private final DutyRepository dutyRepository;
    private final MemberDutyRepository memberDutyRepository;

    @Override
    public void save(MemberDuty memberDuty) {
        MemberJpaEntity memberJpaEntity = memberRepository.getReferenceById(memberDuty.getId().getMemberId());
        Duty duty = dutyRepository.getReferenceById(memberDuty.getId().getDutyId());

        MemberDutyJpaEntity jpaEntity = MemberDutyJpaEntity.builder()
                .member(memberJpaEntity)
                .duty(duty)
                .build();

        memberDutyRepository.save(jpaEntity);
    }
}
