package com.dangbun.domain.checkList.repository;

import com.dangbun.domain.checkList.entity.CheckList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckListRepository extends JpaRepository<CheckList, Long> {

    @Query("select ch from CheckList ch join fetch Cleaning c where c.duty.dutyId = :dutyId")
    List<CheckList> findWithCleaningByDutyId(Long dutyId);

    List<CheckList> findWithCleaning_CleaningId(Long cleaningId);
}
