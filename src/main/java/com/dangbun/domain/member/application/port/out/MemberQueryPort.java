package com.dangbun.domain.member.application.port.out;

import com.dangbun.domain.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberQueryPort {

    List<Member> findByPlaceId(Long placeId);

    List<Member> findWaitingMembersByPlaceId(Long placeId);

    Optional<Member> findByMemberIdAndPlaceId(Long memberId, Long placeId);

    Optional<Member> findByPlaceIdAndName(Long placeId, String name);

    List<Member> findAllByNameIn(List<String> members);

    List<Member> findAllByIds(List<Long> memberIds);

    Page<Member> findByPlaceIdWithPageable(Long placeId, Pageable pageable);

    Page<Member> findByPlaceIdAndNameContaining(Long placeId, String searchName, Pageable pageable);

    Member findById(Long memberId);

    List<Member> findByUserId(Long userId);

    Optional<Member> findByUserIdAndPlaceId(Long userId, Long placeId);

    Optional<Member> findFirstByPlaceId(Long placeId);
}
