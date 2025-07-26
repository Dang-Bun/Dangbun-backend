package com.dangbun.domain.duty.repository;

import com.dangbun.domain.duty.entity.Duty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DutyRepository extends JpaRepository<Duty, Long> {
    boolean existsByNameAndPlace_PlaceId(String name, Long placeId);
    List<Duty> findByPlace_PlaceId(Long placeId);
    Optional<Duty> findByName(String name);
}
