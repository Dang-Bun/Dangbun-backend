package com.dangbun.domain.cleaningdate.adapter.out.persistence;

import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CleaningDateRepository extends JpaRepository<CleaningDateJpaEntity,Long> {
    void deleteAllByCleaningJpaEntity_CleaningId(Long cleaningId);

    List<CleaningDateJpaEntity> findByCleaningJpaEntity(CleaningJpaEntity cleaningJpaEntity);
}
