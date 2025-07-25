package com.dangbun.domain.cleaning.repository;

import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.duty.entity.Duty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CleaningRepository extends JpaRepository<Cleaning, Long> {
    List<Cleaning> findAllByDuty(Duty duty);

    @Query("""
    SELECT c FROM Cleaning c
    JOIN MemberCleaning mc ON c.cleaningId = mc.cleaning.cleaningId
    WHERE c.duty.dutyId = :dutyId AND mc.member.memberId IN :memberIds
    """)
    List<Cleaning> findByDutyIdAndMemberIds(Long dutyId, List<Long> memberIds);
}
