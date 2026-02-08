package com.dangbun.domain.member.application.port.service;

import com.dangbun.domain.duty.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.duty.domain.DutyIcon;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.application.port.in.command.AssignDutyCommand;
import com.dangbun.domain.member.application.port.in.command.ExitPlaceCommand;
import com.dangbun.domain.member.application.port.in.command.RemoveMemberCommand;
import com.dangbun.domain.member.application.port.out.MemberCommandPort;
import com.dangbun.domain.member.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.member.exception.custom.*;
import com.dangbun.domain.memberduty.application.port.out.MemberDutyCommandPort;
import com.dangbun.domain.memberduty.adapter.out.persistence.SpringDataMemberDutyRepository;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.global.context.MemberContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceTest {

    @InjectMocks
    private MemberCommandService memberCommandService;

    @Mock
    private MemberCommandPort memberCommandPort;

    @Mock
    private MemberQueryPort memberQueryPort;

    @Mock
    private MemberDutyCommandPort memberDutyCommandPort;

    @Mock
    private DutyQueryPort dutyQueryPort;

    @Mock
    private SpringDataMemberDutyRepository memberDutyRepository;

    private PlaceJpaEntity mockPlace;
    private MemberJpaEntity mockContextMember;

    @BeforeEach
    void setUp() {
        mockPlace = PlaceJpaEntity.builder()
                .name("테스트 플레이스")
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
    }

    @Test
    void 멤버_등록_성공() {
        // given
        Long memberId = 200L;
        Member waitingMember = Member.withId(memberId, com.dangbun.domain.member.domain.MemberRole.WAITING,
                "대기 멤버", false, Map.of(), 1L, 2L, null);

        given(memberQueryPort.findByMemberIdAndPlaceId(memberId, 1L)).willReturn(Optional.of(waitingMember));

        // when
        memberCommandService.registerMember(memberId);

        // then
        then(memberCommandPort).should().save(any(Member.class));
    }

    @Test
    void 멤버_등록_실패_존재하지_않는_멤버() {
        // given
        Long memberId = 999L;

        given(memberQueryPort.findByMemberIdAndPlaceId(memberId, 1L)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> memberCommandService.registerMember(memberId))
                .isInstanceOf(MemberNotFoundException.class);

        then(memberCommandPort).should(never()).save(any());
    }

    @Test
    void 대기_멤버_삭제_성공() {
        // given
        Long memberId = 200L;
        Member waitingMember = Member.withId(memberId, com.dangbun.domain.member.domain.MemberRole.WAITING,
                "대기 멤버", false, Map.of(), 1L, 2L, null);

        given(memberQueryPort.findByMemberIdAndPlaceId(memberId, 1L)).willReturn(Optional.of(waitingMember));

        // when
        memberCommandService.removeWaitingMember(memberId);

        // then
        then(memberCommandPort).should().delete(waitingMember);
    }

    @Test
    void 플레이스_나가기_실패_매니저는_나갈_수_없음() {
        // given
        ExitPlaceCommand command = new ExitPlaceCommand("테스트 플레이스");

        // when, then
        assertThatThrownBy(() -> memberCommandService.exitPlace(command))
                .isInstanceOf(InvalidRoleException.class);

        then(memberCommandPort).should(never()).delete(any());
    }

    @Test
    void 플레이스_나가기_성공() {
        // given
        MemberJpaEntity memberContext = MemberJpaEntity.builder()
                .name("일반 멤버")
                .place(mockPlace)
                .role(MemberRole.MEMBER)
                .status(true)
                .build();
        ReflectionTestUtils.setField(memberContext, "memberId", 200L);
        MemberContext.set(memberContext);

        ExitPlaceCommand command = new ExitPlaceCommand("테스트 플레이스");
        Member member = Member.withId(200L, com.dangbun.domain.member.domain.MemberRole.MEMBER,
                "일반 멤버", true, Map.of(), 1L, 2L, null);

        given(memberQueryPort.findByMemberIdAndPlaceId(200L, 1L)).willReturn(Optional.of(member));

        // when
        memberCommandService.exitPlace(command);

        // then
        then(memberCommandPort).should().delete(member);
    }

    @Test
    void 플레이스_나가기_실패_플레이스_이름_불일치() {
        // given
        MemberJpaEntity memberContext = MemberJpaEntity.builder()
                .name("일반 멤버")
                .place(mockPlace)
                .role(MemberRole.MEMBER)
                .status(true)
                .build();
        ReflectionTestUtils.setField(memberContext, "memberId", 200L);
        MemberContext.set(memberContext);

        ExitPlaceCommand command = new ExitPlaceCommand("잘못된 플레이스 이름");

        // when, then
        assertThatThrownBy(() -> memberCommandService.exitPlace(command))
                .isInstanceOf(NameNotMatchedException.class);

        then(memberCommandPort).should(never()).delete(any());
    }

    @Test
    void 멤버_제거_성공() {
        // given
        Long memberId = 200L;
        RemoveMemberCommand command = new RemoveMemberCommand(memberId, "타겟 멤버");
        Member targetMember = Member.withId(memberId, com.dangbun.domain.member.domain.MemberRole.MEMBER,
                "타겟 멤버", true, Map.of(), 1L, 2L, null);

        given(memberQueryPort.findByMemberIdAndPlaceId(memberId, 1L)).willReturn(Optional.of(targetMember));

        // when
        memberCommandService.removeMember(command);

        // then
        then(memberCommandPort).should().delete(targetMember);
    }

    @Test
    void 멤버_제거_실패_이름_불일치() {
        // given
        Long memberId = 200L;
        RemoveMemberCommand command = new RemoveMemberCommand(memberId, "잘못된 이름");
        Member targetMember = Member.withId(memberId, com.dangbun.domain.member.domain.MemberRole.MEMBER,
                "타겟 멤버", true, Map.of(), 1L, 2L, null);

        given(memberQueryPort.findByMemberIdAndPlaceId(memberId, 1L)).willReturn(Optional.of(targetMember));

        // when, then
        assertThatThrownBy(() -> memberCommandService.removeMember(command))
                .isInstanceOf(NameNotMatchedException.class);

        then(memberCommandPort).should(never()).delete(any());
    }

    @Test
    void 당번_할당_성공() {
        // given
        Long memberId = 200L;
        Long dutyId = 300L;
        AssignDutyCommand command = new AssignDutyCommand(memberId, dutyId);

        Member targetMember = Member.withId(memberId, com.dangbun.domain.member.domain.MemberRole.MEMBER,
                "타겟 멤버", true, Map.of(), 1L, 2L, null);
        Duty duty = Duty.withId(new Duty.DutyId(dutyId), "청소 당번", DutyIcon.BROOM, 1L);

        given(memberQueryPort.findByMemberIdAndPlaceId(memberId, 1L)).willReturn(Optional.of(targetMember));
        given(dutyQueryPort.findByIdAndPlaceId(dutyId, 1L)).willReturn(Optional.of(duty));
        given(memberDutyRepository.existsByDuty_DutyIdAndMember_MemberId(dutyId, memberId)).willReturn(false);

        // when
        memberCommandService.assignDutyToMember(command);

        // then
        then(memberDutyCommandPort).should().save(any());
    }

    @Test
    void 당번_할당_실패_이미_할당됨() {
        // given
        Long memberId = 200L;
        Long dutyId = 300L;
        AssignDutyCommand command = new AssignDutyCommand(memberId, dutyId);

        Member targetMember = Member.withId(memberId, com.dangbun.domain.member.domain.MemberRole.MEMBER,
                "타겟 멤버", true, Map.of(), 1L, 2L, null);
        Duty duty = Duty.withId(new Duty.DutyId(dutyId), "청소 당번", DutyIcon.BROOM, 1L);

        given(memberQueryPort.findByMemberIdAndPlaceId(memberId, 1L)).willReturn(Optional.of(targetMember));
        given(dutyQueryPort.findByIdAndPlaceId(dutyId, 1L)).willReturn(Optional.of(duty));
        given(memberDutyRepository.existsByDuty_DutyIdAndMember_MemberId(dutyId, memberId)).willReturn(true);

        // when, then
        assertThatThrownBy(() -> memberCommandService.assignDutyToMember(command))
                .isInstanceOf(MemberDutyAlreadyAssignedException.class);

        then(memberDutyCommandPort).should(never()).save(any());
    }

    @Test
    void 당번_할당_실패_당번이_플레이스에_없음() {
        // given
        Long memberId = 200L;
        Long dutyId = 999L;
        AssignDutyCommand command = new AssignDutyCommand(memberId, dutyId);

        Member targetMember = Member.withId(memberId, com.dangbun.domain.member.domain.MemberRole.MEMBER,
                "타겟 멤버", true, Map.of(), 1L, 2L, null);

        given(memberQueryPort.findByMemberIdAndPlaceId(memberId, 1L)).willReturn(Optional.of(targetMember));
        given(dutyQueryPort.findByIdAndPlaceId(dutyId, 1L)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> memberCommandService.assignDutyToMember(command))
                .isInstanceOf(DutyNotInPlaceException.class);

        then(memberDutyCommandPort).should(never()).save(any());
    }
}
