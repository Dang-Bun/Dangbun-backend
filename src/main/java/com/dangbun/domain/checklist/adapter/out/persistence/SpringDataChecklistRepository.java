package com.dangbun.domain.checklist.adapter.out.persistence;

import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataChecklistRepository extends JpaRepository<ChecklistJpaEntity, Long> {

    @Query("select ch from ChecklistJpaEntity ch join fetch ch.cleaningJpaEntity c where c.duty.dutyId = :dutyId")
    List<ChecklistJpaEntity> findWithCleaningByDutyId(Long dutyId);

    List<ChecklistJpaEntity> findByCleaningJpaEntity_CleaningId(Long cleaningId);

    @Query("select ch from ChecklistJpaEntity ch join fetch CleaningJpaEntity c where ch.checklistId = :checklistId")
    Optional<ChecklistJpaEntity> findWithCleaningById(Long checklistId);

    @Query("select ch from ChecklistJpaEntity ch join fetch CleaningJpaEntity c join fetch DutyJpaEntity d where ch.checklistId = :checklistId")
    Optional<ChecklistJpaEntity> findWithCleaningAndDutyById(Long checklistId);

    @Query("SELECT c FROM ChecklistJpaEntity c " +
            "JOIN c.cleaningJpaEntity cl " +
            "JOIN MemberCleaningJpaEntity mc ON cl.cleaningId = mc.cleaningJpaEntity.cleaningId " +
            "JOIN MemberJpaEntity m ON mc.member.memberId = m.memberId " +
            "WHERE c.checklistId = :checklistId AND m.memberId = :memberId")
    Optional<ChecklistJpaEntity> findByChecklistAndMemberId(@Param("checklistId") Long checklistId, @Param("memberId") Long memberId);


    Boolean existsByCleaningJpaEntityAndCreatedAt(@NotNull CleaningJpaEntity cleaningJpaEntity, LocalDateTime createdAt);

    @Query("""
            select case when count(ch) > 0 then true else false end
            from ChecklistJpaEntity ch
            where ch.cleaningJpaEntity.cleaningId = :cleaningId
            and DATE(ch.createdAt) = DATE(:createdAt)
            """)
    boolean existsByCleaningIdAndCreatedAtDate(@Param("cleaningId") Long cleaningId, @Param("createdAt") LocalDateTime createdAt);

    @Query("""
            select ch from ChecklistJpaEntity ch
            join fetch ch.cleaningJpaEntity c
            join fetch c.duty d
            where ch.createdAt >= :start and ch.createdAt < :end
            and c.place.placeId = :placeId
            """)
    List<ChecklistJpaEntity> findAllByCreatedDateAndPlaceId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("placeId") Long placeId);

    @Query("""
            select case when count(ch)>0 then true else false end
            from ChecklistJpaEntity ch
            where ch.createdAt >= :start and ch.createdAt< :end
            and ch.cleaningJpaEntity = :cleaningJpaEntity
            and ch.isComplete = true
            """)
    boolean existsCompletedChecklistByDateAndCleaning(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("cleaningJpaEntity") CleaningJpaEntity cleaningJpaEntity);


    @Query("""
            select ch from ChecklistJpaEntity ch
            where ch.cleaningJpaEntity.place.placeId = :placeId 
            and ch.createdAt >= :startDateTime
            and ch.createdAt < :endDateTime
            """)
    List<ChecklistJpaEntity> findByPlaceAndMonth(@Param("placeId") Long placeId,
                                                 @Param("startDateTime") LocalDateTime startDateTime,
                                                 @Param("endDateTime") LocalDateTime endDateTime);

}
