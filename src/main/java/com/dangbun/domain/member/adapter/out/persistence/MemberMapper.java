package com.dangbun.domain.member.adapter.out.persistence;

import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.member.domain.MemberRole;
/*
 * TODO: Place 도메인 헥사고날 아키텍처 전환 완료 후 수정 필요
 * - import 변경: com.dangbun.domain.place.original.repository.PlaceRepository
 *   -> com.dangbun.domain.place.refactor.application.port.out.PlaceQueryPort
 * - PlaceRepository 대신 PlaceQueryPort 사용
 * - mapToJpaEntity에서 placeRepository.findById() 대신
 *   PlaceQueryPort.findById()를 사용하고 PlaceJpaEntity로 변환
 * - Member 도메인 헥사고날 아키텍처 전환 시 MemberCommandPort를 통해
 *   Place 의존성을 완전히 분리하는 것을 권장
 * - MemberRole 변환 로직은 original.entity.MemberRole 제거 후 삭제
 */
import com.dangbun.domain.place.adapter.out.persistence.SpringDataPlaceRepository;
import com.dangbun.domain.user.adapter.out.persistence.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MemberMapper {

    private final SpringDataPlaceRepository placeRepository;
    private final SpringDataUserRepository userRepository;

    public MemberJpaEntity mapToJpaEntity(Member member) {
        return new MemberJpaEntity(
                toJpaRole(member.getRole()),
                member.getName(),
                member.getStatus(),
                member.getInformation(),
                placeRepository.findById(member.getPlaceId()).get(),
                userRepository.findById(member.getUserId()).get()
        );
    }

    public Member mapToDomainEntity(MemberJpaEntity member) {
        return Member.withId(
                member.getMemberId(),
                toDomainRole(member.getRole()),
                member.getName(),
                member.getStatus(),
                member.getInformation(),
                member.getPlace().getPlaceId(),
                member.getUserJpaEntity().getUserId(),
                member.getCreatedAt()
        );
    }

    private com.dangbun.domain.member.adapter.out.persistence.MemberRole toJpaRole(MemberRole role) {
        return com.dangbun.domain.member.adapter.out.persistence.MemberRole.valueOf(role.name());
    }

    private MemberRole toDomainRole(com.dangbun.domain.member.adapter.out.persistence.MemberRole role) {
        return MemberRole.valueOf(role.name());
    }
}
