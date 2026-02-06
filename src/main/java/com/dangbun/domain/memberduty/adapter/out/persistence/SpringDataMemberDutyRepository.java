package com.dangbun.domain.memberduty.adapter.out.persistence;

import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataMemberDutyRepository extends JpaRepository<MemberDutyJpaEntity, MemberDutyJpaEntityId> {

    @Query("SELECT md FROM MemberDutyJpaEntity md JOIN FETCH md.member m JOIN md.duty d WHERE m.place.placeId = :placeId")
    List<MemberDutyJpaEntity> findAllWithMemberByPlaceId(Long placeId);

    List<MemberDutyJpaEntity> findAllByDuty_DutyId(Long dutyId);

    List<MemberDutyJpaEntity> findAllByDuty(DutyJpaEntity duty);

    @Query("SELECT md FROM MemberDutyJpaEntity md JOIN FETCH md.duty WHERE md.member.memberId = :memberId")
    List<MemberDutyJpaEntity> findAllByMemberId(Long memberId);

    @Query("SELECT md FROM MemberDutyJpaEntity md JOIN FETCH md.duty WHERE md.member.memberId = :memberId")
    List<MemberDutyJpaEntity> findAllByMember_MemberId(Long memberId);

    @Query("SELECT md FROM MemberDutyJpaEntity md JOIN FETCH md.duty WHERE md.member = :member")
    List<MemberDutyJpaEntity> findAllByMember(MemberJpaEntity member);

    @Query("SELECT md.member.memberId FROM MemberDutyJpaEntity md WHERE md.duty.dutyId = :dutyId")
    List<Long> findMemberIdsByDutyId(Long dutyId);

    @Query("SELECT md.member FROM MemberDutyJpaEntity md WHERE md.duty = :duty")
    List<MemberJpaEntity> findMembersByDuty(DutyJpaEntity duty);

    boolean existsByDuty_DutyIdAndMember_MemberId(Long dutyId, Long memberId);

    boolean existsByDutyAndMember(DutyJpaEntity duty, MemberJpaEntity member);

    @Query("""
        SELECT DISTINCT md.duty
        FROM MemberDutyJpaEntity md
        WHERE md.id.memberId IN :memberIds
    """)
    List<DutyJpaEntity> findDistinctDutiesByMemberIds(List<Long> memberIds);

    @Modifying
    @Query("DELETE FROM MemberDutyJpaEntity md WHERE md.duty.dutyId = :dutyId")
    void deleteAllByDutyId(Long dutyId);

    @Modifying
    @Query("DELETE FROM MemberDutyJpaEntity md WHERE md.member.memberId = :memberId AND md.duty.dutyId = :dutyId")
    void deleteByMemberIdAndDutyId(Long memberId, Long dutyId);
}
