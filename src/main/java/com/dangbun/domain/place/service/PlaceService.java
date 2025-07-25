package com.dangbun.domain.place.service;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.exception.custom.InvalidRoleException;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.place.dto.request.PostCheckInviteCodeRequest;
import com.dangbun.domain.place.dto.request.PostCreatePlaceRequest;
import com.dangbun.domain.place.dto.request.PostRegisterPlaceRequest;
import com.dangbun.domain.place.dto.response.GetPlaceListResponse;
import com.dangbun.domain.place.dto.response.PostCheckInviteCodeResponse;
import com.dangbun.domain.place.dto.response.PostCreateInviteCodeResponse;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.entity.PlaceCategory;
import com.dangbun.domain.place.exception.custom.AlreadyInvitedException;
import com.dangbun.domain.place.exception.custom.InvalidInformationException;
import com.dangbun.domain.place.exception.custom.InvalidInviteCodeException;
import com.dangbun.domain.place.repository.PlaceRepository;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.UserNotFoundException;
import com.dangbun.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.dangbun.domain.member.response.status.MemberExceptionResponse.INVALID_ROLE;
import static com.dangbun.domain.place.response.status.PlaceExceptionResponse.*;
import static com.dangbun.domain.user.response.status.UserExceptionResponse.NO_SUCH_USER;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceService {


    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;


    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional(readOnly = true)
    public List<GetPlaceListResponse> getPlaces(Long userId) {

        List<Member> members = memberRepository.findWithPlaceByUserId(userId);
        List<GetPlaceListResponse> result = new ArrayList<>();
        for (Member member : members) {
            Place place = member.getPlace();
            Long placeId = place.getPlaceId();
            String name = place.getName();
            String role = member.getRole().getDisplayName();
            // Todo cleaning과 알림창과의 연동 필요
            result.add(new GetPlaceListResponse(placeId, name, 0, 0, role, 0));
        }
        return result;
    }

    public void createPlaceWithManager(Long userId, PostCreatePlaceRequest request) {
        
        
        String placeName = request.placeName();
        String category = request.category();
        String memberName = request.memberName();
        Map<String, String> info = request.information();

        System.out.println("placeName = " + placeName);
        System.out.println("memberName = " + memberName);
        System.out.println("info = " + info);
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
                .status(true)
                .user(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(NO_SUCH_USER)))
                .build();

        memberRepository.save(member);
    }

    public PostCreateInviteCodeResponse createInviteCode(Long userId, Long placeId) {
        Member member = memberRepository.findWithPlaceByUserIdAndPlaceId(userId, placeId);
        if(member.getRole().equals(MemberRole.MEMBER)){
            throw new InvalidRoleException(INVALID_ROLE);
        }

        Place place = member.getPlace();
        String code = place.createCode(generateCode());

        return new PostCreateInviteCodeResponse(code);
    }



    @Transactional(readOnly = true)
    public PostCheckInviteCodeResponse checkInviteCode(User user, PostCheckInviteCodeRequest request) {
        Place place = placeRepository.findByInviteCode(request.inviteCode());
        if(place == null){
            throw new InvalidInviteCodeException(NO_SUCH_INVITE);
        }
        if(memberRepository.findByPlaceAndUser(place,user).isPresent()){
            throw new AlreadyInvitedException(ALREADY_INVITED);
        }
        Member member = memberRepository.findFirstByPlace(place);
        Set<String> information = member.getInformation().keySet();
        List<String> iList = information.stream().toList();

        return PostCheckInviteCodeResponse.of(request.inviteCode(), iList);
    }

    public static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    public void joinRequest(User user, PostRegisterPlaceRequest request) {


        Member tempMember = memberRepository.findFirstWithPlaceByInviteCode(request.inviteCode())
                .orElseThrow(()->new EntityNotFoundException("Invalid Invite Code"));

        Place place = tempMember.getPlace();

        if(!tempMember.getInformation().keySet().equals(request.information().keySet())){
            throw new InvalidInformationException(INVALID_INFORMATION);
        }

        Member member = Member.builder()
                .user(user)
                .role(MemberRole.MEMBER)
                .status(false)
                .place(place)
                .name(request.name())
                .information(request.information())
                .build();

        memberRepository.save(member);
    }
}
