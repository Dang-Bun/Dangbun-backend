package com.dangbun.domain.cleaningdate.repository;

import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaningdate.entity.CleaningDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CleaningDateRepository extends JpaRepository<CleaningDate,Long> {
    void deleteAllByCleaning_CleaningId(Long cleaningId);
    void deleteAllByCleaningIn(List<Cleaning> cleanings);

    List<CleaningDate> findByCleaning(Cleaning cleaning);
}
