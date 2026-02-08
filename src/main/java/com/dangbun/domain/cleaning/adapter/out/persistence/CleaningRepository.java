package com.dangbun.domain.cleaning.adapter.out.persistence;

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
import com.dangbun.domain.duty.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CleaningRepository extends JpaRepository<CleaningJpaEntity, Long> {
    List<CleaningJpaEntity> findAllByDuty(DutyJpaEntity duty);



    boolean existsByNameAndDuty_DutyIdAndPlace_PlaceId(String name, Long dutyDutyId, Long placePlaceId);


    boolean existsByNameAndDuty_DutyIdAndCleaningIdNotAndPlace_PlaceId(String name, Long dutyDutyId, Long cleaningId, Long placePlaceId);

    @Query("""
            SELECT c FROM CleaningJpaEntity c
            JOIN c.duty d
            JOIN MemberCleaningJpaEntity mc ON c.cleaningId = mc.cleaningJpaEntity.cleaningId
            WHERE d.dutyId = :dutyId AND mc.member.memberId IN :memberIds
            """)
    List<CleaningJpaEntity> findByDutyIdAndMemberIdsWithMembersJoin(Long dutyId, List<Long> memberIds);

    @Query("SELECT c FROM CleaningJpaEntity c LEFT JOIN FETCH c.duty WHERE c.cleaningId = :cleaningId")
    Optional<CleaningJpaEntity> findWithDutyNullableById(Long cleaningId);


    Optional<CleaningJpaEntity> findByCleaningIdAndDuty_DutyId(Long cleaningId, Long dutyId);

    @Query("""
    SELECT c
    FROM CleaningJpaEntity c
    WHERE c.duty IS NULL AND c.place.placeId = :placeId
    """)
    List<CleaningJpaEntity> findUnassignedCleaningsByPlaceId(Long placeId);

    List<CleaningJpaEntity> findByPlace(PlaceJpaEntity place);

    boolean existsByNameAndDuty_DutyIdAndPlace(String name, Long dutyDutyId, PlaceJpaEntity place);
}