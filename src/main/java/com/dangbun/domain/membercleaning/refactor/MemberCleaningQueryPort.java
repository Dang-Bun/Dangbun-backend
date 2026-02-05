package com.dangbun.domain.membercleaning.refactor;

import com.dangbun.domain.duty.refactor.domain.Duty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface MemberCleaningQueryPort {
    List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds);
}
