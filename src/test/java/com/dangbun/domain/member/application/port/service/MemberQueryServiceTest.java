package com.dangbun.domain.member.application.port.service;

import com.dangbun.domain.member.adapter.out.persistence.FakeMemberRepository;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.member.domain.MemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MemberQueryServiceTest {

    private MemberQueryService memberQueryService;
    private FakeMemberRepository fakeMemberRepository;

    @BeforeEach
    void setUp() {
        fakeMemberRepository = new FakeMemberRepository();

        memberQueryService = new MemberQueryService(fakeMemberRepository);
    }

    @Test
    void 사용자ID로_멤버_목록_조회_성공() {
        // given
        Long userId = 1L;
        fakeMemberRepository.save(
                Member.withoutId(MemberRole.MANAGER, "매니저", true, Map.of(), 10L, userId)
        );
        fakeMemberRepository.save(
                Member.withoutId(MemberRole.MEMBER, "멤버", true, Map.of(), 20L, userId)
        );

        // when
        List<Member> result = memberQueryService.getMembersByUserId(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Member::getPlaceId).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    void 사용자ID로_멤버_목록_조회_빈_목록() {
        // given
        Long userId = 999L;

        // when
        List<Member> result = memberQueryService.getMembersByUserId(userId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 다른_사용자_멤버는_조회되지_않음() {
        // given
        fakeMemberRepository.save(
                Member.withoutId(MemberRole.MANAGER, "유저1", true, Map.of(), 10L, 1L)
        );
        fakeMemberRepository.save(
                Member.withoutId(MemberRole.MEMBER, "유저2", true, Map.of(), 20L, 2L)
        );

        // when
        List<Member> result = memberQueryService.getMembersByUserId(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("유저1");
    }

    @Test
    void 사용자ID와_플레이스ID로_멤버_조회_성공() {
        // given
        Long userId = 1L;
        Long placeId = 10L;
        fakeMemberRepository.save(
                Member.withoutId(MemberRole.MANAGER, "매니저", true, Map.of(), placeId, userId)
        );

        // when
        Optional<Member> result = memberQueryService.getMemberByUserIdAndPlaceId(userId, placeId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getRole()).isEqualTo(MemberRole.MANAGER);
    }

    @Test
    void 사용자ID와_플레이스ID로_멤버_조회_없음() {
        // given
        fakeMemberRepository.save(
                Member.withoutId(MemberRole.MANAGER, "매니저", true, Map.of(), 10L, 1L)
        );

        // when
        Optional<Member> result = memberQueryService.getMemberByUserIdAndPlaceId(1L, 999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 플레이스의_첫번째_멤버_조회_성공() {
        // given
        Long placeId = 10L;
        fakeMemberRepository.save(
                Member.withoutId(MemberRole.MANAGER, "첫번째 멤버", true, Map.of("phone", "010-1234-5678"), placeId, 1L)
        );
        fakeMemberRepository.save(
                Member.withoutId(MemberRole.MEMBER, "두번째 멤버", true, Map.of(), placeId, 2L)
        );

        // when
        Optional<Member> result = memberQueryService.getFirstMemberByPlaceId(placeId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getInformation()).containsKey("phone");
    }

    @Test
    void 플레이스의_첫번째_멤버_조회_빈_플레이스() {
        // given
        Long placeId = 999L;

        // when
        Optional<Member> result = memberQueryService.getFirstMemberByPlaceId(placeId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 여러_플레이스에_가입된_사용자_조회() {
        // given
        Long userId = 1L;
        fakeMemberRepository.save(
                Member.withoutId(MemberRole.MANAGER, "유저1-플레이스1", true, Map.of(), 10L, userId)
        );
        fakeMemberRepository.save(
                Member.withoutId(MemberRole.MEMBER, "유저1-플레이스2", true, Map.of(), 20L, userId)
        );
        fakeMemberRepository.save(
                Member.withoutId(MemberRole.WAITING, "유저1-플레이스3", false, Map.of(), 30L, userId)
        );

        // when
        List<Member> result = memberQueryService.getMembersByUserId(userId);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Member::getRole)
                .containsExactlyInAnyOrder(MemberRole.MANAGER, MemberRole.MEMBER, MemberRole.WAITING);
    }
}
