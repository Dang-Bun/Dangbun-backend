package com.dangbun.domain.member.repository;

import com.dangbun.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m JOIN FETCH m.place WHERE m.user.id = :userId")
    List<Member> findWithPlaceByUserId(Long userId);

    @Query("select m from Member m join fetch m.place where m.user.id = :userId and m.place.placeId = :placeId")
    Member findWithPlaceByUserIdAndPlaceId(Long userId, Long placeId);

}
