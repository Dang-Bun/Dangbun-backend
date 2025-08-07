package com.dangbun.domain.duty.repository;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.place.dto.response.DutyProgressDto;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DutyRepository extends JpaRepository<Duty, Long> {
    boolean existsByNameAndPlace_PlaceId(String name, Long placeId);

    List<Duty> findByPlace_PlaceId(Long placeId);

    Optional<Duty> findByName(String name);

    @Query("select d from Duty d join fetch d.place p where p.placeId = :placeId")
    List<Duty> findWithPlaceByPlaceId(@Param("placeId") Long placeId);

    @Query("""
    SELECT new com.dangbun.domain.place.dto.response.DutyProgressDto(
        d.dutyId,
        d.name,
        COUNT(cl),
        SUM(CASE WHEN cl.isComplete = true THEN 1 ELSE 0 END)
    )
    FROM Checklist cl
    JOIN cl.cleaning c
    JOIN c.duty d
    WHERE d.place.placeId = :placeId
      AND cl.createdAt = CURRENT_DATE
    GROUP BY d.dutyId, d.name
""")
    List<DutyProgressDto> findDutyProgressByPlaceToday(@Param("placeId") Long placeId);
}
