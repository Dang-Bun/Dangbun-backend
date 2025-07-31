package com.dangbun.domain.notification.service;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.notification.dto.response.GetMemberSearchListResponse;
import com.dangbun.domain.notification.dto.response.GetMemberSearchListResponse.MemberDto;
import com.dangbun.domain.notification.exception.custom.PlaceNotFoundException;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dangbun.domain.notification.response.status.NotificationExceptionResponse.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;

    public GetMemberSearchListResponse searchMembers(Long placeId, String searchName, Pageable pageable) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new PlaceNotFoundException(PLACE_NOT_FOUND));


        Page<Member> memberPage;

        if (searchName == null || searchName.isBlank()) {
            memberPage = memberRepository.findByPlace_PlaceId(placeId, pageable);
        } else {
            memberPage = memberRepository.findByPlace_PlaceIdAndUser_NameContaining(placeId, searchName, pageable);
        }

        List<MemberDto> memberDtos = memberPage.getContent().stream()
                .map(MemberDto::of)
                .toList();

        return new GetMemberSearchListResponse(memberDtos, memberPage.hasNext());
    }
}

