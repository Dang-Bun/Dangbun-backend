package com.dangbun.domain.memberduty.refactor.adapter.out;

import com.dangbun.domain.memberduty.refactor.MemberDuty;
import org.springframework.stereotype.Component;

@Component
class MemberDutyMapper {
    public MemberDuty mapToDomainEntity(MemberDutyJpaEntity jpaEntity){
        return new MemberDuty(jpaEntity.getMember(), jpaEntity.getDuty());
    }
}
