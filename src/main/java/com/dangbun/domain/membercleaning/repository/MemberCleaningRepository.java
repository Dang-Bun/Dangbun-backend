package com.dangbun.domain.membercleaning.repository;

import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.membercleaning.entity.MemberCleaningId;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberCleaningRepository extends JpaRepository<MemberCleaning, MemberCleaningId> {
    @Query("""
        SELECT DISTINCT mc.cleaning.duty
        FROM MemberCleaning mc
        WHERE mc.id.memberId IN :memberIds
    """)
    List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds);

    List<MemberCleaning> findAllByCleaning(Cleaning cleaning);

    List<MemberCleaning> findAllByMember(Member member);

    void deleteAllByMember(Member member);
}
