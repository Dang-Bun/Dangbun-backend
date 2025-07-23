package com.dangbun.domain.place.service;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.member.service.MemberService;
import com.dangbun.domain.place.dto.request.PostCreatePlaceRequest;
import com.dangbun.domain.place.dto.response.GetPlaceListResponse;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.entity.PlaceCategory;
import com.dangbun.domain.place.repository.PlaceRepository;
import com.dangbun.domain.user.exception.UserErrorCode;
import com.dangbun.domain.user.exception.custom.UserNotFoundException;
import com.dangbun.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaceService {


    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final UserRepository userRepository;

    public List<GetPlaceListResponse> getPlaces(Long userId) {

        List<Member> members = memberRepository.findWithPlaceByUserId(userId);
        List<GetPlaceListResponse> result = new ArrayList<>();
        for (Member member : members) {
            Place place = member.getPlace();
            String name = place.getName();
            String role = member.getRole().getDisplayName();
            // Todo cleaning과 알림창과의 연동 필요
            result.add(new GetPlaceListResponse(name, 0, 0, role, 0));
        }
        return result;
    }

    public void createPlaceWithManager(Long userId, PostCreatePlaceRequest request) {

        String placeName = request.placeName();
        String category = request.category();
        String memberName = request.memberName();
        Map<String, String> info = request.memberInformation();



        Place place = Place.builder()
                .name(placeName)
                .category(PlaceCategory.findCategory(category))
                .build();

        Place savedPlace = placeRepository.save(place);

        Member member = Member.builder()
                .name(memberName)
                .place(savedPlace)
                .information(info)
                .role(MemberRole.MANAGER)
                .user(userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(UserErrorCode.NO_SUCH_USER)))
                .build();

        memberRepository.save(member);
    }
}
