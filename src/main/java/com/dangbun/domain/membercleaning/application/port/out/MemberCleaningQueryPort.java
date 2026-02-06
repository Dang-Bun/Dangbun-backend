package com.dangbun.domain.membercleaning.application.port.out;

import com.dangbun.domain.duty.refactor.domain.Duty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface MemberCleaningQueryPort {
    List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds);

    List<String> findMemberNamesByCleaningId(Long cleaningId);
}
