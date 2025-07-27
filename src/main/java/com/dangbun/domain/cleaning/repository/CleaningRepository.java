package com.dangbun.domain.cleaning.repository;

import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.duty.entity.Duty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CleaningRepository extends JpaRepository<Cleaning, Long> {
    List<Cleaning> findAllByDuty(Duty duty);


    boolean existsByNameAndDuty(String name, Duty duty);

    boolean existsByNameAndDutyAndCleaningIdNot(String name, Duty duty, Long cleaningId);

    @Query("""
        SELECT DISTINCT c FROM Cleaning c
        JOIN FETCH c.memberCleanings mc
        JOIN FETCH mc.member
        WHERE c.duty.dutyId = :dutyId AND c.cleaningId IN (
            SELECT c2.cleaningId FROM Cleaning c2
            JOIN c2.memberCleanings mc2
            WHERE mc2.member.memberId IN :memberIds
        )
    """)
    List<Cleaning> findByDutyIdAndMemberIdsWithMembersJoin(Long dutyId, List<Long> memberIds);

    @Query("SELECT c FROM Cleaning c LEFT JOIN FETCH c.duty WHERE c.cleaningId = :cleaningId")
    Optional<Cleaning> findWithDutyNullableById(Long cleaningId);
}
