package com.dangbun.domain.member.repository;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m JOIN FETCH m.place WHERE m.user.userId = :userId")
    List<Member> findWithPlaceByUserId(Long userId);

    @Query("select m from Member m join fetch m.place where m.user.userId = :userId and m.place.placeId = :placeId")
    Optional<Member> findWithPlaceByUserIdAndPlaceId(Long userId, Long placeId);

    Optional<Member> findByPlaceAndUser(Place place, User user);

    Member findFirstByPlace(Place place);

    @Query("select m from Member m join fetch m.place p where p.inviteCode = :inviteCode")
    List<Member> findWithPlaceByInviteCode(@Param("inviteCode") String inviteCode);

    List<Member> findAllByNameIn(List<String> names);

    List<Member> findByPlace_PlaceId(Long placeId);

    List<Member> findByPlace_PlaceIdAndStatusIsFalseOrderByNameAsc(Long placeId);

    Optional<Member> findByMemberIdAndPlace_PlaceId(Long memberId, Long placeId);

    Optional<Member> findByPlace_PlaceIdAndName(Long placeId, String name);

    Page<Member> findByPlace_PlaceId(Long placeId, Pageable pageable);

    Page<Member> findByPlace_PlaceIdAndNameContaining(Long placeId, String name, Pageable pageable);

    List<Member> findALLByUser(User user);

}
