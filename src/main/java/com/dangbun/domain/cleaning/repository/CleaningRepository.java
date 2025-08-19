package com.dangbun.domain.cleaning.repository;

import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CleaningRepository extends JpaRepository<Cleaning, Long> {
    List<Cleaning> findAllByDuty(Duty duty);


    boolean existsByNameAndDutyAndPlace(String name, Duty duty, Place place);

    boolean existsByNameAndDutyAndCleaningIdNotAndPlace(String name, Duty duty, Long cleaningId, Place place);

    @Query("""
            SELECT c FROM Cleaning c
            JOIN c.duty d
            JOIN MemberCleaning mc ON c.cleaningId = mc.cleaning.cleaningId
            WHERE d.dutyId = :dutyId AND mc.member.memberId IN :memberIds
            """)
    List<Cleaning> findByDutyIdAndMemberIdsWithMembersJoin(Long dutyId, List<Long> memberIds);

    @Query("SELECT c FROM Cleaning c LEFT JOIN FETCH c.duty WHERE c.cleaningId = :cleaningId")
    Optional<Cleaning> findWithDutyNullableById(Long cleaningId);


    Optional<Cleaning> findByCleaningIdAndDuty_DutyId(Long cleaningId, Long dutyId);

    @Query("""
    SELECT c
    FROM Cleaning c
    WHERE c.duty IS NULL AND c.place.placeId = :placeId
    """)
    List<Cleaning> findUnassignedCleaningsByPlaceId(Long placeId);

}