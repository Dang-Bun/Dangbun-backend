package com.dangbun.domain.cleaningdate.repository;

import com.dangbun.domain.cleaningdate.entity.CleaningDate;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CleaningDateRepository extends JpaRepository<CleaningDate,Long> {
    void deleteAllByCleaning_CleaningId(Long cleaningId);
}
