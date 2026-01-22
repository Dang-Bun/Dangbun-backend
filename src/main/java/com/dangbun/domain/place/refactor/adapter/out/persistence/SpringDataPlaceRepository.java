package com.dangbun.domain.place.refactor.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPlaceRepository extends JpaRepository<PlaceJpaEntity, Long> {
}
