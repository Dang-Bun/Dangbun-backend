package com.dangbun.domain.membercleaning.application.port.out;

import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.member.domain.Member;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface MemberCleaningQueryPort {
    List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds);

    List<String> findMemberNamesByCleaningId(Long cleaningId);

    List<Member> findMembersByCleaningId(Long cleaningId);

    List<Long> findCleaningIdsByMemberId(Long memberId);

    Integer countCleaningsByMemberId(Long memberId);

    /**
     * Cleaning에 할당된 멤버 수 조회 (Duty 도메인에서 사용)
     */
    Integer countMembersByCleaningId(Long cleaningId);
}
