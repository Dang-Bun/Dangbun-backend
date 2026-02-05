package com.dangbun.domain.member.refactor.application.port.out;

import com.dangbun.domain.member.refactor.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberQueryPort {

    List<Member> findByPlaceId(Long placeId);

    List<Member> findWaitingMembersByPlaceId(Long placeId);

    Optional<Member> findByMemberIdAndPlaceId(Long memberId, Long placeId);

    Optional<Member> findByPlaceIdAndName(Long placeId, String name);

    List<Member> findAllByNameIn(List<String> members);
}
