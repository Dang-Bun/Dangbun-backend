package com.dangbun.domain.memberduty.repository;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberDutyRepository extends JpaRepository<MemberDuty, Long> {

    List<MemberDuty> findAllByDuty(Duty duty);
}
