package com.dangbun.domain.duty.refactor.adapter.out.persistence;

import com.dangbun.domain.place.adapter.in.web.dto.response.DutyProgressDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataDutyRepository extends JpaRepository<DutyJpaEntity, Long> {

    boolean existsByNameAndPlace_PlaceId(String name, Long placeId);

    List<DutyJpaEntity> findByPlace_PlaceId(Long placeId);

    Optional<DutyJpaEntity> findByName(String name);

    @Query("SELECT d FROM DutyJpaEntity d JOIN FETCH d.place p WHERE p.placeId = :placeId")
    List<DutyJpaEntity> findWithPlaceByPlaceId(@Param("placeId") Long placeId);

    @Query("""
            SELECT new com.dangbun.domain.place.adapter.in.web.dto.response.DutyProgressDto(
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
