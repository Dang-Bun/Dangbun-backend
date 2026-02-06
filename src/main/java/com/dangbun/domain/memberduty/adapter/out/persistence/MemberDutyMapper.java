package com.dangbun.domain.memberduty.adapter.out.persistence;

import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.duty.refactor.adapter.out.persistence.SpringDataDutyRepository;
import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRepository;
import com.dangbun.domain.memberduty.domain.MemberDuty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MemberDutyMapper {

    /*
     * TODO: Member 도메인 헥사고날 아키텍처 전환 완료 후 수정
     * MemberRepository -> SpringDataMemberRepository
     */
    private final MemberRepository memberRepository;
    private final SpringDataDutyRepository dutyRepository;

    public MemberDutyJpaEntity mapToJpaEntity(MemberDuty memberDuty) {
        MemberJpaEntity member = memberRepository.getReferenceById(memberDuty.getMemberId());
        DutyJpaEntity duty = dutyRepository.getReferenceById(memberDuty.getDutyId());

        return MemberDutyJpaEntity.builder()
                .member(member)
                .duty(duty)
                .build();
    }

    public MemberDuty mapToDomainEntity(MemberDutyJpaEntity jpaEntity) {
        return MemberDuty.of(
                jpaEntity.getMember().getMemberId(),
                jpaEntity.getDuty().getDutyId()
        );
    }

    public Duty mapDutyToDomainEntity(DutyJpaEntity dutyJpaEntity) {
        return Duty.withId(
                new Duty.DutyId(dutyJpaEntity.getDutyId()),
                dutyJpaEntity.getName(),
                dutyJpaEntity.getIcon(),
                dutyJpaEntity.getPlace().getPlaceId()
        );
    }
}
