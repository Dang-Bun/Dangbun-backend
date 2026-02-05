package com.dangbun.domain.membercleaning.refactor;

import com.dangbun.domain.membercleaning.refactor.domain.MemberCleaning;

import java.util.List;

public interface MemberCleaningCommandPort {
    void saveAll(List<MemberCleaning> memberCleanings);
}
