package com.dangbun.domain.user.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findBySocialId(String socialId);

    boolean existsByEmail(String email);
}
