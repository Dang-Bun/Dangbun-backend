package com.dangbun.domain.member.repository;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m JOIN FETCH m.place WHERE m.user.userId = :userId")
    List<Member> findWithPlaceByUserId(Long userId);

    @Query("select m from Member m join fetch m.place where m.user.userId = :userId and m.place.placeId = :placeId")
    Member findWithPlaceByUserIdAndPlaceId(Long userId, Long placeId);

    Optional<Member> findByPlaceAndUser(Place place, User user);

    Member findFirstByPlace(Place place);

    @Query("select m from Member m join fetch m.place p where p.inviteCode = :inviteCode")
    Optional<Member> findFirstWithPlaceByInviteCode(@Param("inviteCode") String inviteCode);

    List<Member> findAllByNameIn(List<String> names);

    List<Member> findByPlace_PlaceIdAndStatusIsTrue(Long placeId);

    @Query("select count(m)>0 from Member m where m.user.userId = :userId and m.place.placeId = :placeId")
    boolean existsByUserIdAndPlaceId(@Param("userId") Long userId, @Param("placeId") Long placeId);


    Optional<Member> findByUser_UserIdAndPlace_PlaceId(Long userId, Long placePlaceId);

    List<Member> findByPlace_PlaceIdAndStatusIsFalse(Long placeId);

    Optional<Member> findByMemberIdAndPlace_PlaceId(Long memberId, Long placeId);

    Optional<Member> findByMemberId(Long memberId);

}
