package com.dangbun.domain.notification.service;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.member.service.MemberService;
import com.dangbun.domain.notification.dto.response.GetMemberSearchListResponse;
import com.dangbun.domain.notification.dto.response.GetMemberSearchListResponse.MemberDto;
import com.dangbun.domain.notification.dto.response.GetRecentSearchResponse;
import com.dangbun.domain.place.repository.PlaceRepository;
import com.dangbun.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;
    private final RedisService redisService;
    private static final int MAX_RECENT_COUNT = 5;
    private final MemberService memberService;


    public GetMemberSearchListResponse searchMembers(Long userId, Long placeId, String searchName, Pageable pageable) {
        Long memberId = memberService.getMemberByUserAndPlace(userId,placeId).getMemberId();

        Page<Member> memberPage;
        if (searchName == null || searchName.isBlank()) {
            memberPage = memberRepository.findByPlace_PlaceId(placeId, pageable);
        } else {
            String redisKey = redisService.getRedisKey(placeId, memberId);
            redisService.addRecentSearch(redisKey, searchName, MAX_RECENT_COUNT);
            memberPage = memberRepository.findByPlace_PlaceIdAndNameContaining(placeId, searchName, pageable);
        }

        List<MemberDto> memberDtos = memberPage.getContent().stream()
                .map(MemberDto::of)
                .toList();

        return GetMemberSearchListResponse.of(memberDtos, memberPage.hasNext());
    }

    public GetRecentSearchResponse getRecentSearches(Long userId, Long placeId) {
        Long memberId = memberService.getMemberByUserAndPlace(userId,placeId).getMemberId();
        String redisKey = redisService.getRedisKey(placeId, memberId);
        List<String> recentSearches = redisService.getRecentSearches(redisKey, MAX_RECENT_COUNT);
        return GetRecentSearchResponse.of(recentSearches);
    }



}

