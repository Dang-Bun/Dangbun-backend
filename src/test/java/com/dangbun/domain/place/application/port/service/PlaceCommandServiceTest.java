package com.dangbun.domain.place.application.port.service;

import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.application.port.out.GetMemberByInviteCodePort;
import com.dangbun.domain.member.application.port.out.MemberCommandPort;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.domain.place.application.port.in.command.CreatePlaceCommand;
import com.dangbun.domain.place.application.port.in.command.JoinPlaceCommand;
import com.dangbun.domain.place.application.port.in.command.UpdateTimeCommand;
import com.dangbun.domain.place.application.port.in.command.UpdateTimeResult;
import com.dangbun.domain.place.application.port.out.PlaceCommandPort;
import com.dangbun.domain.place.application.port.out.PlaceQueryPort;
import com.dangbun.domain.place.domain.Place;
import com.dangbun.domain.place.domain.PlaceCategory;
import com.dangbun.domain.place.exception.custom.InvalidInformationException;
import com.dangbun.domain.place.exception.custom.InvalidPlaceNameException;
import com.dangbun.domain.place.exception.custom.InvalidTimeException;
import com.dangbun.domain.user.adapter.out.persistence.UserJpaEntity;
import com.dangbun.global.context.MemberContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PlaceCommandServiceTest {

    @InjectMocks
    private PlaceCommandService placeCommandService;

    @Mock
    private PlaceCommandPort placeCommandPort;

    @Mock
    private PlaceQueryPort placeQueryPort;

    @Mock
    private MemberCommandPort memberCommandPort;

    @Mock
    private GetMemberByInviteCodePort getMemberByInviteCodePort;

    private PlaceJpaEntity mockPlace;
    private MemberJpaEntity mockContextMember;
    private UserJpaEntity mockUserJpaEntity;

    @BeforeEach
    void setUp() {
        mockPlace = PlaceJpaEntity.builder()
                .name("테스트 카페")
                .build();
        ReflectionTestUtils.setField(mockPlace, "placeId", 1L);

        mockUserJpaEntity = UserJpaEntity.builder()
                .name("테스트유저")
                .email("test@test.com")
                .password("password123")
                .enabled(true)
                .build();
        ReflectionTestUtils.setField(mockUserJpaEntity, "userId", 1L);

        mockContextMember = MemberJpaEntity.builder()
                .name("매니저")
                .place(mockPlace)
                .role(MemberRole.MANAGER)
                .status(true)
                .user(mockUserJpaEntity)
                .build();
        ReflectionTestUtils.setField(mockContextMember, "memberId", 100L);

        MemberContext.set(mockContextMember);
    }

    @AfterEach
    void tearDown() {
        MemberContext.clear();
    }

    @Test
    void 플레이스_생성_성공() {
        // given
        CreatePlaceCommand command = new CreatePlaceCommand(
                1L,
                "새 카페",
                PlaceCategory.CAFE,
                null,
                "매니저 이름",
                Map.of("phone", "010-1234-5678")
        );

        Place savedPlace = new Place(
                new Place.PlaceId(10L),
                "새 카페",
                PlaceCategory.CAFE,
                "카페",
                "abc123",
                null,
                null,
                null
        );

        given(placeCommandPort.save(any(Place.class))).willReturn(savedPlace);

        // when
        Long placeId = placeCommandService.createPlaceWithManager(command);

        // then
        assertThat(placeId).isEqualTo(10L);
        then(placeCommandPort).should().save(any(Place.class));
        then(memberCommandPort).should().save(any(Member.class));
    }

    @Test
    void 플레이스_생성_기타_카테고리_성공() {
        // given
        CreatePlaceCommand command = new CreatePlaceCommand(
                1L,
                "기타 장소",
                PlaceCategory.ETC,
                "커스텀 카테고리",
                "매니저 이름",
                Map.of("phone", "010-1234-5678")
        );

        Place savedPlace = new Place(
                new Place.PlaceId(10L),
                "기타 장소",
                PlaceCategory.ETC,
                "커스텀 카테고리",
                "abc123",
                null,
                null,
                null
        );

        given(placeCommandPort.save(any(Place.class))).willReturn(savedPlace);

        // when
        Long placeId = placeCommandService.createPlaceWithManager(command);

        // then
        assertThat(placeId).isEqualTo(10L);
    }

    @Test
    void 초대코드_생성_성공() {
        // given
        Place place = new Place(
                new Place.PlaceId(1L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                null,
                null,
                null,
                null
        );

        given(placeQueryPort.findById(1L)).willReturn(Optional.of(place));
        given(placeCommandPort.save(any(Place.class))).willReturn(place);

        // when
        String code = placeCommandService.createInviteCode();

        // then
        assertThat(code).isNotNull();
        assertThat(code).hasSize(6);
        then(placeCommandPort).should().save(any(Place.class));
    }

    @Test
    void 플레이스_가입_요청_성공() {
        // given
        JoinPlaceCommand command = new JoinPlaceCommand(
                1L,
                "abc123",
                "새 멤버",
                Map.of("phone", "010-9999-9999")
        );

        Member existingMember = Member.withId(
                100L,
                com.dangbun.domain.member.domain.MemberRole.MANAGER,
                "기존 멤버",
                true,
                Map.of("phone", "010-1234-5678"),
                10L,
                2L,
                null
        );

        given(getMemberByInviteCodePort.getMember("abc123")).willReturn(existingMember);

        // when
        Long placeId = placeCommandService.joinPlaceRequest(command);

        // then
        assertThat(placeId).isEqualTo(10L);
        then(memberCommandPort).should().save(any(Member.class));
    }

    @Test
    void 플레이스_가입_요청_실패_정보_불일치() {
        // given
        JoinPlaceCommand command = new JoinPlaceCommand(
                1L,
                "abc123",
                "새 멤버",
                Map.of("email", "test@test.com")  // phone 대신 email
        );

        Member existingMember = Member.withId(
                100L,
                com.dangbun.domain.member.domain.MemberRole.MANAGER,
                "기존 멤버",
                true,
                Map.of("phone", "010-1234-5678"),
                10L,
                2L,
                null
        );

        given(getMemberByInviteCodePort.getMember("abc123")).willReturn(existingMember);

        // when, then
        assertThatThrownBy(() -> placeCommandService.joinPlaceRequest(command))
                .isInstanceOf(InvalidInformationException.class);

        then(memberCommandPort).should(never()).save(any());
    }

    @Test
    void 플레이스_삭제_성공() {
        // given
        Place place = new Place(
                new Place.PlaceId(1L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                "abc123",
                null,
                null,
                null
        );

        given(placeQueryPort.findById(1L)).willReturn(Optional.of(place));

        // when
        placeCommandService.deletePlace("테스트 카페");

        // then
        then(placeCommandPort).should().delete(place);
    }

    @Test
    void 플레이스_삭제_실패_이름_불일치() {
        // given
        Place place = new Place(
                new Place.PlaceId(1L),
                "테스트 카페",
                PlaceCategory.CAFE,
                "카페",
                "abc123",
                null,
                null,
                null
        );

        given(placeQueryPort.findById(1L)).willReturn(Optional.of(place));

        // when, then
        assertThatThrownBy(() -> placeCommandService.deletePlace("잘못된 이름"))
                .isInstanceOf(InvalidPlaceNameException.class);

        then(placeCommandPort).should(never()).delete(any());
    }

    @Test
    void 시간_업데이트_성공() {
        // given
        UpdateTimeCommand command = new UpdateTimeCommand(
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                true
        );

        // when
        UpdateTimeResult result = placeCommandService.updateTime(command);

        // then
        assertThat(result.startTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.endTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(result.isToday()).isTrue();
        then(placeCommandPort).should().updateTime(any());
    }

    @Test
    void 시간_업데이트_실패_시작시간이_종료시간보다_늦음() {
        // given
        UpdateTimeCommand command = new UpdateTimeCommand(
                LocalTime.of(18, 0),
                LocalTime.of(9, 0),
                true
        );

        // when, then
        assertThatThrownBy(() -> placeCommandService.updateTime(command))
                .isInstanceOf(InvalidTimeException.class);

        then(placeCommandPort).should(never()).updateTime(any());
    }

    @Test
    void 가입_취소_성공() {
        // when
        placeCommandService.cancelRegister();

        // then
        then(memberCommandPort).should().delete(any(Member.class));
    }
}
