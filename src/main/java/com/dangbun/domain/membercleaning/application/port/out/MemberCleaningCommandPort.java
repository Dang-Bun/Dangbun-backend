package com.dangbun.domain.membercleaning.application.port.out;

import com.dangbun.domain.membercleaning.domain.MemberCleaning;

import java.util.List;

public interface MemberCleaningCommandPort {
    void saveAll(List<MemberCleaning> memberCleanings);

    void deleteAllByCleaningId(Long cleaningId);
}
