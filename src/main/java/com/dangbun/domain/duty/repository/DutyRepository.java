package com.dangbun.domain.duty.repository;

import com.dangbun.domain.duty.entity.Duty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DutyRepository extends JpaRepository<Duty, Long> {
    boolean existsByNameAndPlace_PlaceId(String name, Long placeId);
    List<Duty> findByPlace_PlaceId(Long placeId);

}
