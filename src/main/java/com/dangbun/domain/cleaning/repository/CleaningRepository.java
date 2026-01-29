package com.dangbun.domain.cleaning.repository;

import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.duty.entity.Duty;
/*
 * TODO: Place 도메인 헥사고날 아키텍처 전환 완료 후 수정 필요
 * - import 변경: com.dangbun.domain.place.original.entity.Place
 *   -> com.dangbun.domain.place.refactor.adapter.out.persistence.PlaceJpaEntity
 * - 메서드 파라미터 타입 변경:
 *   - existsByNameAndDutyAndPlace(..., Place place) -> existsByNameAndDutyAndPlace(..., PlaceJpaEntity place)
 *   - existsByNameAndDutyAndCleaningIdNotAndPlace(..., Place place) -> ... PlaceJpaEntity place
 *   - findByPlace(Place place) -> findByPlace(PlaceJpaEntity place)
 * - 또는 placeId 기반 쿼리로 변경하여 엔티티 의존성 제거 고려
 */
import com.dangbun.domain.place.original.entity.Place;
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

    List<Cleaning> findByPlace(Place place);
}