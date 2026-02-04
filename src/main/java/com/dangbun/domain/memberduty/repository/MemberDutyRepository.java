package com.dangbun.domain.memberduty.repository;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.memberduty.entity.MemberDutyJpaEntity;
import com.dangbun.domain.memberduty.entity.MemberDutyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberDutyRepository extends JpaRepository<MemberDutyJpaEntity, MemberDutyId> {

    List<MemberDutyJpaEntity> findAllByDuty(Duty duty);

    @Query("select md from MemberDutyJpaEntity md join fetch md.duty where md.member = :member")
    List<MemberDutyJpaEntity> findAllByMember(MemberJpaEntity member);

    @Query("select md from MemberDutyJpaEntity md join fetch md.duty where md.member.memberId = :memberId")
    List<MemberDutyJpaEntity> findAllByMember_MemberId(Long memberId);

    boolean existsByDutyAndMember(Duty duty, MemberJpaEntity member);

    boolean existsByDuty_DutyIdAndMember_MemberId(Long dutyDutyId, Long memberMemberId);

    @Query("SELECT md.member FROM MemberDutyJpaEntity md WHERE md.duty = :duty")
    List<MemberJpaEntity> findMembersByDuty(Duty duty);


    @Query("select md from MemberDutyJpaEntity md join fetch md.member m join md.duty d where m.place.placeId = :placeId")
    List<MemberDutyJpaEntity> findAllWithMemberAndPlaceByPlaceId(Long placeId);

    void deleteAllByDuty(Duty duty);
}
