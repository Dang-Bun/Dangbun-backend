package com.dangbun.domain.membercleaning.application.port.in.query;

import java.util.List;

public interface GetCleaningInfoByMemberQuery {

    List<Long> getCleaningIdsByMemberId(Long memberId);

    Integer getCleaningCountByMemberId(Long memberId);
}
