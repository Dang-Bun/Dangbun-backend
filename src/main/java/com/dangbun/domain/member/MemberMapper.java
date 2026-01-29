package com.dangbun.domain.member;

import com.dangbun.domain.member.entity.MemberJpaEntity;
/*
 * TODO: Place 도메인 헥사고날 아키텍처 전환 완료 후 수정 필요
 * - import 변경: com.dangbun.domain.place.original.repository.PlaceRepository
 *   -> com.dangbun.domain.place.refactor.application.port.out.PlaceQueryPort
 * - PlaceRepository 대신 PlaceQueryPort 사용
 * - mapToJpaEntity에서 placeRepository.findById() 대신
 *   PlaceQueryPort.findById()를 사용하고 PlaceJpaEntity로 변환
 * - Member 도메인 헥사고날 아키텍처 전환 시 MemberCommandPort를 통해
 *   Place 의존성을 완전히 분리하는 것을 권장
 */
import com.dangbun.domain.place.original.repository.PlaceRepository;
import com.dangbun.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MemberMapper {

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
