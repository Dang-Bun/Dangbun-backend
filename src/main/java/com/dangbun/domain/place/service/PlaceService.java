package com.dangbun.domain.place.service;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.place.dto.GetPlaceListResponse;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {


    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;

    public List<GetPlaceListResponse> getPlaces(Long userId) {

        List<Member> members = memberRepository.findWithPlaceByUserId(userId);
        List<GetPlaceListResponse> result = new ArrayList<>();
        for (Member member : members) {
            Place place = member.getPlace();
            String name = place.getName();
            String role = member.getRole().getDisplayName();
            result.add(new GetPlaceListResponse(name, 0, 0, role, 0));
        }
        return result;
    }
}
