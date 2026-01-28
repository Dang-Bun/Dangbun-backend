package com.dangbun.domain.member;

import com.dangbun.domain.member.entity.MemberJpaEntity;
import com.dangbun.domain.place.original.repository.PlaceRepository;
import com.dangbun.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MemberMapper {


    // Todo 의존성 제거
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    public MemberJpaEntity mapToJpaEntity(Member member) {
        return new MemberJpaEntity(
                member.getRole(),
                member.getName(),
                member.getStatus(),
                member.getInformation(),
                placeRepository.findById(member.getPlaceId()).get(),
                userRepository.findById(member.getUserId()).get()
        );
    }

    public Member mapToDomainEntity(MemberJpaEntity member) {
        return  Member.withIdBuilder()
                .memberId(member.getMemberId())
                .role(member.getRole())
                .name(member.getName())
                .status(member.getStatus())
                .information(member.getInformation())
                .placeId(member.getPlace().getPlaceId())
                .userId(member.getUser().getUserId())
                .build();
    }
}
