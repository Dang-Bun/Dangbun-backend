package com.dangbun.domain.place.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPlaceRepository extends JpaRepository<PlaceJpaEntity, Long> {
    Optional<PlaceJpaEntity> findByInviteCode(String inviteCode);
}
