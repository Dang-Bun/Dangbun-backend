package com.dangbun.domain.member.application.port.in.query;

import com.dangbun.common.hexagonal.UseCase;

@UseCase
public interface MemberQuery {

    MembersResult getMembers();

    MemberDetailResult getMember(Long memberId);

    WaitingMembersResult getWaitingMembers();

    MyInformationResult getMyInformation();

    MemberSearchResult searchByNameInPlace(Long placeId, String name);
}
