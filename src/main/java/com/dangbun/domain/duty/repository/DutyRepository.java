package com.dangbun.domain.duty.repository;

import com.dangbun.domain.duty.entity.Duty;
import io.lettuce.core.dynamic.annotation.Param;
import org.hibernate.annotations.Parameter;
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
}
