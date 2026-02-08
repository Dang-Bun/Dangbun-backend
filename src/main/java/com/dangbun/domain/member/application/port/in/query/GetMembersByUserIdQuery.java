package com.dangbun.domain.member.application.port.in.query;

import com.dangbun.domain.member.domain.Member;

import java.util.List;
import java.util.Optional;

public interface GetMembersByUserIdQuery {

    List<Member> getMembersByUserId(Long userId);

    Optional<Member> getMemberByUserIdAndPlaceId(Long userId, Long placeId);

    Optional<Member> getFirstMemberByPlaceId(Long placeId);
}
