package com.dangbun.domain.memberduty.repository;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.entity.MemberDutyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberDutyRepository extends JpaRepository<MemberDuty, MemberDutyId> {

    List<MemberDuty> findAllByDuty(Duty duty);

    @Query("select md from MemberDuty md join fetch md.duty where md.member = :member")
    List<MemberDuty> findAllByMember(Member member);

    boolean existsByDutyAndMember(Duty duty, Member member);

    @Query("SELECT md.member FROM MemberDuty md WHERE md.duty = :duty")
    List<Member> findMembersByDuty(Duty duty);


    @Query("select md from MemberDuty md join fetch md.member m join md.duty d where m.place.placeId = :placeId")
    List<MemberDuty> findAllWithMemberAndPlaceByPlaceId(Long placeId);

    void deleteAllByMember(Member member);

    @Query("""
    SELECT md.member, md.duty
    FROM MemberDuty md
    JOIN FETCH md.member m
    JOIN FETCH md.duty d
    JOIN FETCH d.place
    WHERE d.dutyId = :dutyId AND m.user.userId = :userId
    """)
    Optional<MemberDuty> findMemberDutyWithDutyAndPlace(Long dutyId, Long userId);

}
