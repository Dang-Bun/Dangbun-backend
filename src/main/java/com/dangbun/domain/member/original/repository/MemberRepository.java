package com.dangbun.domain.member.original.repository;

/*
 * TODO: Place 도메인 헥사고날 아키텍처 전환 완료 후 수정 필요
 * - import 변경: com.dangbun.domain.place.original.entity.Place
 *   -> com.dangbun.domain.place.refactor.adapter.out.persistence.PlaceJpaEntity
 * - 메서드 파라미터 타입 변경:
 *   - findByPlaceAndUser(Place place, User user) -> findByPlaceAndUser(PlaceJpaEntity place, User user)
 *   - findFirstByPlace(Place place) -> findFirstByPlace(PlaceJpaEntity place)
 * - 또는 placeId 기반 쿼리로 변경하여 엔티티 의존성 제거 고려
 */
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
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

    Optional<MemberJpaEntity> findByPlaceAndUser(PlaceJpaEntity place, User user);

    MemberJpaEntity findFirstByPlace(PlaceJpaEntity place);

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
