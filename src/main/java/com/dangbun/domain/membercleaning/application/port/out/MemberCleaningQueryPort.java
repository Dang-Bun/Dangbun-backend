package com.dangbun.domain.membercleaning.application.port.out;

import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.member.domain.Member;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface MemberCleaningQueryPort {
    List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds);

    List<String> findMemberNamesByCleaningId(Long cleaningId);

    List<Member> findMembersByCleaningId(Long cleaningId);
}
