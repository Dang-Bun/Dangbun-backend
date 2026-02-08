package com.dangbun.domain.member.application.port.service;

import com.dangbun.domain.duty.application.port.in.query.FakeGetDutyForMemberQuery;
import com.dangbun.domain.member.adapter.out.persistence.FakeMemberRepository;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.application.port.in.command.AssignDutyCommand;
import com.dangbun.domain.member.application.port.in.command.ExitPlaceCommand;
import com.dangbun.domain.member.application.port.in.command.RemoveMemberCommand;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.member.exception.custom.*;
import com.dangbun.domain.memberduty.application.port.in.command.FakeMemberDutyForMemberUseCase;
import com.dangbun.domain.memberduty.application.port.in.query.FakeGetMemberDutyForMemberQuery;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.domain.place.domain.PlaceCategory;
import com.dangbun.global.context.MemberContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MemberCommandService 테스트 - Fake 객체 사용
 * Mock 대신 인메모리 Fake 저장소를 사용하여 실제 동작을 검증
 */
class MemberCommandServiceWithFakeTest {

    private MemberCommandService memberCommandService;

    private FakeMemberRepository fakeMemberRepository;
    private FakeGetDutyForMemberQuery fakeGetDutyForMemberQuery;
    private FakeGetMemberDutyForMemberQuery fakeGetMemberDutyForMemberQuery;
    private FakeMemberDutyForMemberUseCase fakeMemberDutyForMemberUseCase;

    private PlaceJpaEntity mockPlace;
    private MemberJpaEntity mockContextMember;

    @BeforeEach
    void setUp() {
        // Fake 저장소 초기화
        fakeMemberRepository = new FakeMemberRepository();
        fakeGetDutyForMemberQuery = new FakeGetDutyForMemberQuery();
        fakeGetMemberDutyForMemberQuery = new FakeGetMemberDutyForMemberQuery();
        fakeMemberDutyForMemberUseCase = new FakeMemberDutyForMemberUseCase();

        // 서비스 생성
        memberCommandService = new MemberCommandService(
                fakeMemberRepository,
                fakeMemberRepository,
                fakeGetDutyForMemberQuery,
                fakeGetMemberDutyForMemberQuery,
                fakeMemberDutyForMemberUseCase
        );

        // MemberContext 설정
        mockPlace = PlaceJpaEntity.builder()
                .name("테스트 카페")
                .category(PlaceCategory.CAFE)
                .build();
        ReflectionTestUtils.setField(mockPlace, "placeId", 1L);

        mockContextMember = MemberJpaEntity.builder()
                .name("매니저")
                .place(mockPlace)
                .role(MemberRole.MANAGER)
                .status(true)
                .build();
        ReflectionTestUtils.setField(mockContextMember, "memberId", 100L);

        MemberContext.set(mockContextMember);
    }

    @AfterEach
    void tearDown() {
        MemberContext.clear();
        fakeMemberRepository.clear();
        fakeGetDutyForMemberQuery.clear();
        fakeGetMemberDutyForMemberQuery.clear();
        fakeMemberDutyForMemberUseCase.clear();
    }

    @Test
    void 멤버_활성화_성공() {
        // given
        Member waitingMember = createWaitingMember(1L, "대기 멤버", 1L);
        fakeMemberRepository.save(waitingMember);

        // when
        memberCommandService.registerMember(1L);

        // then
        Member activatedMember = fakeMemberRepository.findByMemberIdAndPlaceId(1L, 1L).orElseThrow();
        assertThat(activatedMember.getStatus()).isTrue();
        assertThat(activatedMember.getRole()).isEqualTo(com.dangbun.domain.member.domain.MemberRole.MEMBER);
    }

    @Test
    void 멤버_활성화_실패_멤버가_존재하지_않음() {
        // given
        // 멤버가 없는 상태

        // when, then
        assertThatThrownBy(() -> memberCommandService.registerMember(999L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void 대기_멤버_삭제_성공() {
        // given
        Member waitingMember = createWaitingMember(1L, "대기 멤버", 1L);
        fakeMemberRepository.save(waitingMember);

        // when
        memberCommandService.removeWaitingMember(1L);

        // then
        assertThat(fakeMemberRepository.findByMemberIdAndPlaceId(1L, 1L)).isEmpty();
    }

    @Test
    void 대기_멤버_삭제_실패_멤버가_존재하지_않음() {
        // given
        // 멤버가 없는 상태

        // when, then
        assertThatThrownBy(() -> memberCommandService.removeWaitingMember(999L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void 플레이스_탈퇴_성공() {
        // given
        mockContextMember = MemberJpaEntity.builder()
                .name("일반 멤버")
                .place(mockPlace)
                .role(MemberRole.MEMBER)
                .status(true)
                .build();
        ReflectionTestUtils.setField(mockContextMember, "memberId", 100L);
        MemberContext.set(mockContextMember);

        Member member = createActiveMember(100L, "일반 멤버", 1L);
        fakeMemberRepository.save(member);

        ExitPlaceCommand command = new ExitPlaceCommand("테스트 카페");

        // when
        memberCommandService.exitPlace(command);

        // then
        assertThat(fakeMemberRepository.findByMemberIdAndPlaceId(100L, 1L)).isEmpty();
    }

    @Test
    void 플레이스_탈퇴_실패_매니저는_탈퇴불가() {
        // given
        ExitPlaceCommand command = new ExitPlaceCommand("테스트 카페");

        // when, then
        assertThatThrownBy(() -> memberCommandService.exitPlace(command))
                .isInstanceOf(InvalidRoleException.class);
    }

    @Test
    void 플레이스_탈퇴_실패_플레이스명_불일치() {
        // given
        mockContextMember = MemberJpaEntity.builder()
                .name("일반 멤버")
                .place(mockPlace)
                .role(MemberRole.MEMBER)
                .status(true)
                .build();
        ReflectionTestUtils.setField(mockContextMember, "memberId", 100L);
        MemberContext.set(mockContextMember);

        ExitPlaceCommand command = new ExitPlaceCommand("잘못된 카페");

        // when, then
        assertThatThrownBy(() -> memberCommandService.exitPlace(command))
                .isInstanceOf(NameNotMatchedException.class);
    }

    @Test
    void 멤버_강제삭제_성공() {
        // given
        Member targetMember = createActiveMember(1L, "삭제할 멤버", 1L);
        fakeMemberRepository.save(targetMember);

        RemoveMemberCommand command = new RemoveMemberCommand(1L, "삭제할 멤버");

        // when
        memberCommandService.removeMember(command);

        // then
        assertThat(fakeMemberRepository.findByMemberIdAndPlaceId(1L, 1L)).isEmpty();
    }

    @Test
    void 멤버_강제삭제_실패_이름_불일치() {
        // given
        Member targetMember = createActiveMember(1L, "실제 이름", 1L);
        fakeMemberRepository.save(targetMember);

        RemoveMemberCommand command = new RemoveMemberCommand(1L, "잘못된 이름");

        // when, then
        assertThatThrownBy(() -> memberCommandService.removeMember(command))
                .isInstanceOf(NameNotMatchedException.class);
    }

    @Test
    void 멤버_강제삭제_실패_멤버가_존재하지_않음() {
        // given
        RemoveMemberCommand command = new RemoveMemberCommand(999L, "없는 멤버");

        // when, then
        assertThatThrownBy(() -> memberCommandService.removeMember(command))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void 업무_할당_성공() {
        // given
        Member targetMember = createActiveMember(1L, "타겟 멤버", 1L);
        fakeMemberRepository.save(targetMember);

        fakeGetDutyForMemberQuery.addDuty(10L, 1L, "청소 당번");

        AssignDutyCommand command = new AssignDutyCommand(1L, 10L);

        // when
        memberCommandService.assignDutyToMember(command);

        // then
        assertThat(fakeMemberDutyForMemberUseCase.existsByMemberIdAndDutyId(1L, 10L)).isTrue();
    }

    @Test
    void 업무_할당_실패_멤버가_존재하지_않음() {
        // given
        fakeGetDutyForMemberQuery.addDuty(10L, 1L, "청소 당번");

        AssignDutyCommand command = new AssignDutyCommand(999L, 10L);

        // when, then
        assertThatThrownBy(() -> memberCommandService.assignDutyToMember(command))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void 업무_할당_실패_업무가_해당_플레이스에_존재하지_않음() {
        // given
        Member targetMember = createActiveMember(1L, "타겟 멤버", 1L);
        fakeMemberRepository.save(targetMember);

        // duty가 없는 상태

        AssignDutyCommand command = new AssignDutyCommand(1L, 999L);

        // when, then
        assertThatThrownBy(() -> memberCommandService.assignDutyToMember(command))
                .isInstanceOf(DutyNotInPlaceException.class);
    }

    @Test
    void 업무_할당_실패_이미_할당된_업무() {
        // given
        Member targetMember = createActiveMember(1L, "타겟 멤버", 1L);
        fakeMemberRepository.save(targetMember);

        fakeGetDutyForMemberQuery.addDuty(10L, 1L, "청소 당번");
        fakeGetMemberDutyForMemberQuery.addMemberDutyAssignment(1L, 10L);

        AssignDutyCommand command = new AssignDutyCommand(1L, 10L);

        // when, then
        assertThatThrownBy(() -> memberCommandService.assignDutyToMember(command))
                .isInstanceOf(MemberDutyAlreadyAssignedException.class);
    }

    @Test
    void 다른_플레이스의_멤버는_조회_불가() {
        // given
        Member otherPlaceMember = createActiveMember(1L, "다른 멤버", 999L); // 다른 placeId
        fakeMemberRepository.save(otherPlaceMember);

        // when, then
        assertThatThrownBy(() -> memberCommandService.registerMember(1L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    private Member createWaitingMember(Long memberId, String name, Long placeId) {
        return Member.withId(
                memberId,
                com.dangbun.domain.member.domain.MemberRole.WAITING,
                name,
                false,
                Map.of("key", "value"),
                placeId,
                1L,
                LocalDateTime.now()
        );
    }

    private Member createActiveMember(Long memberId, String name, Long placeId) {
        return Member.withId(
                memberId,
                com.dangbun.domain.member.domain.MemberRole.MEMBER,
                name,
                true,
                Map.of("key", "value"),
                placeId,
                1L,
                LocalDateTime.now()
        );
    }
}
