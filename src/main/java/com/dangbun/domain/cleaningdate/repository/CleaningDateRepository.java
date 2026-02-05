package com.dangbun.domain.cleaningdate.repository;

import com.dangbun.domain.cleaning.refactor.adapter.out.CleaningJpaEntity;
import com.dangbun.domain.cleaningdate.entity.CleaningDateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CleaningDateRepository extends JpaRepository<CleaningDateJpaEntity,Long> {
    void deleteAllByCleaningJpaEntity_CleaningId(Long cleaningId);

    List<CleaningDateJpaEntity> findByCleaningJpaEntity(CleaningJpaEntity cleaningJpaEntity);
}
