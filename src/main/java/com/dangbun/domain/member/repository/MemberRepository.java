package com.dangbun.domain.member.repository;

import com.dangbun.domain.member.entity.MemberJpaEntity;
import com.dangbun.domain.place.original.entity.Place;
import com.dangbun.domain.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberJpaEntity, Long> {

    @Query("SELECT m FROM MemberJpaEntity m JOIN FETCH m.place WHERE m.user.userId = :userId")
    List<MemberJpaEntity> findWithPlaceByUserId(Long userId);

    @Query("select m from MemberJpaEntity m join fetch m.place where m.user.userId = :userId and m.place.placeId = :placeId")
    Optional<MemberJpaEntity> findWithPlaceByUserIdAndPlaceId(Long userId, Long placeId);

    Optional<MemberJpaEntity> findByPlaceAndUser(Place place, User user);

    MemberJpaEntity findFirstByPlace(Place place);

    @Query("select m from MemberJpaEntity m join fetch m.place p where p.inviteCode = :inviteCode")
    List<MemberJpaEntity> findWithPlaceByInviteCode(@Param("inviteCode") String inviteCode);

    List<MemberJpaEntity> findAllByNameIn(List<String> names);

    List<MemberJpaEntity> findByPlace_PlaceId(Long placeId);

    List<MemberJpaEntity> findByPlace_PlaceIdAndStatusIsFalseOrderByNameAsc(Long placeId);

    Optional<MemberJpaEntity> findByMemberIdAndPlace_PlaceId(Long memberId, Long placeId);

    Optional<MemberJpaEntity> findByPlace_PlaceIdAndName(Long placeId, String name);

    Page<MemberJpaEntity> findByPlace_PlaceId(Long placeId, Pageable pageable);

    Page<MemberJpaEntity> findByPlace_PlaceIdAndNameContaining(Long placeId, String name, Pageable pageable);

    List<MemberJpaEntity> findALLByUser(User user);

}
