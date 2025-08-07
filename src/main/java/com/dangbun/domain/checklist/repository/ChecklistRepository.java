package com.dangbun.domain.checklist.repository;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.cleaning.entity.Cleaning;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    @Query("select ch from Checklist ch join fetch Cleaning c where c.duty.dutyId = :dutyId")
    List<Checklist> findWithCleaningByDutyId(Long dutyId);

    List<Checklist> findByCleaning_CleaningId(Long cleaningId);

    Optional<Checklist> findByChecklistIdAndCleaning_CleaningId(Long checklistId, Long cleaningId);

    Boolean existsByCleaningAndCreatedAt(@NotNull Cleaning cleaning, LocalDateTime createdAt);
}
