package com.dangbun.domain.memberduty.repository;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.entity.MemberDutyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberDutyRepository extends JpaRepository<MemberDuty, MemberDutyId> {

    List<MemberDuty> findAllByDuty(Duty duty);
    boolean existsByDutyAndMember(Duty duty, Member member);

}
