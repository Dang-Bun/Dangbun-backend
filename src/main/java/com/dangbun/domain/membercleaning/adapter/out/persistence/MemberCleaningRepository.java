package com.dangbun.domain.membercleaning.adapter.out.persistence;

import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberCleaningRepository extends JpaRepository<MemberCleaningJpaEntity, MemberCleaningId> {
    @Query("""
        SELECT DISTINCT mc.cleaningJpaEntity.duty
        FROM MemberCleaningJpaEntity mc
        WHERE mc.id.memberId IN :memberIds
    """)
    List<DutyJpaEntity> findDistinctDutiesByMemberIds(List<Long> memberIds);

    List<MemberCleaningJpaEntity> findAllByCleaningJpaEntity(CleaningJpaEntity cleaningJpaEntity);

    List<MemberCleaningJpaEntity> findAllByMember(MemberJpaEntity member);

    List<MemberCleaningJpaEntity> findAllByMember_MemberId(Long memberMemberId);

    @Query("select mc.member from MemberCleaningJpaEntity mc where mc.id.cleaningId = :cleaningId")
    List<MemberJpaEntity> findMembersByCleaningId(Long cleaningId);

    void deleteAllByCleaningJpaEntity_CleaningId(Long cleaningId);
}
