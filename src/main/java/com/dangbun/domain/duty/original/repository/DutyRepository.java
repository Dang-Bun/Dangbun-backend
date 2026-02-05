package com.dangbun.domain.duty.original.repository;

import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.place.refactor.adapter.in.web.dto.response.DutyProgressDto;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DutyRepository extends JpaRepository<DutyJpaEntity, Long> {
    boolean existsByNameAndPlace_PlaceId(String name, Long placeId);

    List<DutyJpaEntity> findByPlace_PlaceId(Long placeId);

    Optional<DutyJpaEntity> findByName(String name);

    @Query("select d from DutyJpaEntity d join fetch d.place p where p.placeId = :placeId")
    List<DutyJpaEntity> findWithPlaceByPlaceId(Long placeId);

    @Query("""
    SELECT new com.dangbun.domain.place.refactor.adapter.in.web.dto.response.DutyProgressDto(
        d.dutyId,
        d.name,
        COUNT(cl),
        SUM(CASE WHEN cl.isComplete = true THEN 1 ELSE 0 END)
    )
    FROM Checklist cl
    JOIN cl.cleaningJpaEntity c
    JOIN c.duty d
    WHERE d.place.placeId = :placeId
      AND cl.createdAt = CURRENT_DATE
    GROUP BY d.dutyId, d.name
""")
    List<DutyProgressDto> findDutyProgressByPlaceToday(@Param("placeId") Long placeId);

    Optional<DutyJpaEntity> findByDutyIdAndPlace_PlaceId(Long dutyId, Long placeId);
}
