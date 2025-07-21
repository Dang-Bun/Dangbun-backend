package com.dangbun.domain.cleaning.repository;

import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.duty.entity.Duty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CleaningRepository extends JpaRepository<Cleaning, Long> {
    List<Cleaning> findAllByDuty(Duty duty);
}
