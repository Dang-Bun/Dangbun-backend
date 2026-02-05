package com.dangbun.domain.memberduty.repository;

import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.memberduty.refactor.adapter.out.MemberDutyJpaEntity;
import com.dangbun.domain.memberduty.entity.MemberDutyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberDutyRepository extends JpaRepository<MemberDutyJpaEntity, MemberDutyId> {

    List<MemberDutyJpaEntity> findAllByDuty(DutyJpaEntity duty);

    @Query("select md from MemberDutyJpaEntity md join fetch md.duty where md.member = :member")
    List<MemberDutyJpaEntity> findAllByMember(MemberJpaEntity member);

    @Query("select md from MemberDutyJpaEntity md join fetch md.duty where md.member.memberId = :memberId")
    List<MemberDutyJpaEntity> findAllByMember_MemberId(Long memberId);

    boolean existsByDutyAndMember(DutyJpaEntity duty, MemberJpaEntity member);

    boolean existsByDuty_DutyIdAndMember_MemberId(Long dutyDutyId, Long memberMemberId);

    @Query("SELECT md.member FROM MemberDutyJpaEntity md WHERE md.duty = :duty")
    List<MemberJpaEntity> findMembersByDuty(DutyJpaEntity duty);


    @Query("select md from MemberDutyJpaEntity md join fetch md.member m join md.duty d where m.place.placeId = :placeId")
    List<MemberDutyJpaEntity> findAllWithMemberAndPlaceByPlaceId(Long placeId);

    void deleteAllByDuty(DutyJpaEntity duty);

    @Query("""
        SELECT DISTINCT md.duty
        FROM MemberDutyJpaEntity md
        WHERE md.id.memberId IN :memberIds
    """)
    List<DutyJpaEntity> findDistinctDutiesByMemberIds(List<Long> memberIds);
}
