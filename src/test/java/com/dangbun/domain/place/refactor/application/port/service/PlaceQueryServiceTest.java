package com.dangbun.domain.place.refactor.application.port.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningRepository;
import com.dangbun.domain.duty.original.entity.Duty;
import com.dangbun.domain.duty.original.repository.DutyRepository;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.member.original.entity.MemberRole;
import com.dangbun.domain.member.original.repository.MemberRepository;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningJpaEntity;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningRepository;
import com.dangbun.domain.memberduty.refactor.adapter.out.MemberDutyJpaEntity;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.domain.notificationreceiver.repository.NotificationReceiverRepository;
import com.dangbun.domain.place.original.entity.Place;
import com.dangbun.domain.place.original.repository.PlaceRepository;
import com.dangbun.domain.place.refactor.adapter.in.web.dto.response.DutyProgressDto;
import com.dangbun.domain.place.refactor.exception.custom.AlreadyInvitedException;
import com.dangbun.domain.place.refactor.exception.custom.InvalidInviteCodeException;
import com.dangbun.domain.place.refactor.exception.custom.InviteCodeNotExistsException;
import com.dangbun.domain.place.refactor.application.port.in.query.DutyProgressResult;
import com.dangbun.domain.place.refactor.application.port.in.query.PlaceListResult;
import com.dangbun.domain.place.refactor.application.port.in.query.PlaceResult;
import com.dangbun.domain.place.refactor.application.port.in.query.PlaceTimeResult;
import com.dangbun.domain.place.refactor.domain.Information;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.context.MemberContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dangbun.domain.place.refactor.domain.PlaceCategory.CAFE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PlaceQueryServiceTest {

    @InjectMocks
    private PlaceQueryService placeQueryService;

    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DutyRepository dutyRepository;
    @Mock
    private ChecklistRepository checklistRepository;
    @Mock
    private CleaningRepository cleaningRepository;
    @Mock
    private MemberCleaningRepository memberCleaningRepository;
    @Mock
    private MemberDutyRepository memberDutyRepository;
    @Mock
    private NotificationReceiverRepository notificationReceiverRepository;

    private User mockUser;
    private Place mockPlace;
    private MemberJpaEntity mockMember;
    private MemberJpaEntity mockManager;
    private Duty mockDuty;
    private CleaningJpaEntity mockCleaningJpaEntity;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .name("테스트유저")
                .email("test@test.com")
                .password("password123")
                .enabled(true)
                .build();
        ReflectionTestUtils.setField(mockUser, "userId", 1L);

        mockPlace = Place.builder()
                .name("테스트 카페")
                .category(CAFE)
                .categoryName("카페")
                .build();
        ReflectionTestUtils.setField(mockPlace, "placeId", 10L);
        mockPlace.createCode("abc123");

        mockMember = MemberJpaEntity.builder()
                .name("홍길동")
                .place(mockPlace)
                .role(MemberRole.MEMBER)
                .status(true)
                .user(mockUser)
                .information(Map.of("phone", "010-1234-5678"))
                .build();
        ReflectionTestUtils.setField(mockMember, "memberId", 100L);

        mockManager = MemberJpaEntity.builder()
                .name("관리자")
                .place(mockPlace)
                .role(MemberRole.MANAGER)
                .status(true)
                .user(mockUser)
                .information(Map.of("phone", "010-9999-9999"))
                .build();
        ReflectionTestUtils.setField(mockManager, "memberId", 200L);

        mockDuty = Duty.builder()
                .name("청소 당번")
                .place(mockPlace)
                .build();
        ReflectionTestUtils.setField(mockDuty, "dutyId", 1000L);

        mockCleaningJpaEntity = CleaningJpaEntity.builder()
                .place(mockPlace)
                .name("화장실 청소")
                .build();
        ReflectionTestUtils.setField(mockCleaningJpaEntity, "cleaningId", 2000L);
    }

    @AfterEach
    void tearDown() {
        MemberContext.clear();
    }

    // ==================== getPlaceList 테스트 ====================

    @Test
    @DisplayName("플레이스 목록 조회 - 대기 중인 멤버")
    void getPlaceList_withWaitingMember() {
        // given
        MemberJpaEntity waitingMember = MemberJpaEntity.builder()
                .name("대기자")
                .place(mockPlace)
                .role(MemberRole.WAITING)
                .status(false)
                .user(mockUser)
                .build();

        given(memberRepository.findWithPlaceByUserId(1L)).willReturn(List.of(waitingMember));

        // when
        PlaceListResult result = placeQueryService.getPlaceList(1L);

        // then
        assertThat(result.places()).hasSize(1);
        PlaceListResult.PlaceDto placeDto = result.places().get(0);
        assertThat(placeDto.placeId()).isEqualTo(10L);
        assertThat(placeDto.name()).isEqualTo("테스트 카페");
        assertThat(placeDto.category()).isEqualTo(CAFE);
        assertThat(placeDto.totalCleaning()).isNull();
        assertThat(placeDto.endCleaning()).isNull();
        assertThat(placeDto.role()).isNull();
    }

    @Test
    @DisplayName("플레이스 목록 조회 - 일반 멤버")
    void getPlaceList_withActiveMember() {
        // given
        MemberCleaningJpaEntity memberCleaningJpaEntity = MemberCleaningJpaEntity.builder()
                .member(mockMember)
                .cleaning(mockCleaningJpaEntity)
                .build();

        given(memberRepository.findWithPlaceByUserId(1L)).willReturn(List.of(mockMember));
        given(memberCleaningRepository.findAllByMember(mockMember)).willReturn(List.of(memberCleaningJpaEntity));
        given(checklistRepository.existsCompletedChecklistByDateAndCleaning(any(), any(), any())).willReturn(false);
        given(notificationReceiverRepository.countUnreadByMemberId(100L)).willReturn(3);

        // when
        PlaceListResult result = placeQueryService.getPlaceList(1L);

        // then
        assertThat(result.places()).hasSize(1);
        PlaceListResult.PlaceDto placeDto = result.places().get(0);
        assertThat(placeDto.placeId()).isEqualTo(10L);
        assertThat(placeDto.totalCleaning()).isEqualTo(1);
        assertThat(placeDto.endCleaning()).isEqualTo(0);
        assertThat(placeDto.role()).isEqualTo("멤버");
        assertThat(placeDto.notifyNumber()).isEqualTo(3);
    }

    @Test
    @DisplayName("플레이스 목록 조회 - 매니저")
    void getPlaceList_withManager() {
        // given
        given(memberRepository.findWithPlaceByUserId(1L)).willReturn(List.of(mockManager));
        given(cleaningRepository.findByPlace(mockPlace)).willReturn(List.of(mockCleaningJpaEntity));
        given(checklistRepository.existsCompletedChecklistByDateAndCleaning(any(), any(), any())).willReturn(true);
        given(notificationReceiverRepository.countUnreadByMemberId(200L)).willReturn(0);

        // when
        PlaceListResult result = placeQueryService.getPlaceList(1L);

        // then
        assertThat(result.places()).hasSize(1);
        PlaceListResult.PlaceDto placeDto = result.places().get(0);
        assertThat(placeDto.totalCleaning()).isEqualTo(1);
        assertThat(placeDto.endCleaning()).isEqualTo(1);
        assertThat(placeDto.role()).isEqualTo("매니저");
    }

    // ==================== checkInviteCode 테스트 ====================

    @Test
    @DisplayName("초대 코드 확인 - 유효한 코드")
    void checkInviteCode_valid() {
        // given
        given(placeRepository.findByInviteCode("abc123")).willReturn(mockPlace);
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
        given(memberRepository.findByPlaceAndUser(mockPlace, mockUser)).willReturn(Optional.empty());
        given(memberRepository.findFirstByPlace(mockPlace)).willReturn(mockMember);

        // when
        Information result = placeQueryService.checkInviteCode(1L, "abc123");

        // then
        assertThat(result.getPlaceId()).isEqualTo(10L);
        assertThat(result.getInformation()).contains("phone");
    }

    @Test
    @DisplayName("초대 코드 확인 - 유효하지 않은 코드")
    void checkInviteCode_invalid() {
        // given
        given(placeRepository.findByInviteCode("wrong")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> placeQueryService.checkInviteCode(1L, "wrong"))
                .isInstanceOf(InvalidInviteCodeException.class);
    }

    @Test
    @DisplayName("초대 코드 확인 - 이미 초대된 사용자")
    void checkInviteCode_alreadyInvited() {
        // given
        given(placeRepository.findByInviteCode("abc123")).willReturn(mockPlace);
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
        given(memberRepository.findByPlaceAndUser(mockPlace, mockUser)).willReturn(Optional.of(mockMember));

        // when & then
        assertThatThrownBy(() -> placeQueryService.checkInviteCode(1L, "abc123"))
                .isInstanceOf(AlreadyInvitedException.class);
    }

    // ==================== getPlace 테스트 ====================

    @Test
    @DisplayName("플레이스 조회 - 대기 중인 멤버")
    void getPlace_waitingMember() {
        // given
        MemberJpaEntity waitingMember = MemberJpaEntity.builder()
                .name("대기자")
                .place(mockPlace)
                .role(MemberRole.WAITING)
                .status(false)
                .build();
        ReflectionTestUtils.setField(waitingMember, "memberId", 300L);
        MemberContext.set(waitingMember);

        // when
        PlaceResult result = placeQueryService.getPlace();

        // then
        assertThat(result.memberId()).isEqualTo(300L);
        assertThat(result.placeId()).isEqualTo(10L);
        assertThat(result.placeName()).isEqualTo("테스트 카페");
        assertThat(result.duty()).isNull();
    }

    @Test
    @DisplayName("플레이스 조회 - 일반 멤버")
    void getPlace_activeMember() {
        // given
        MemberContext.set(mockMember);

        MemberDutyJpaEntity memberDutyJpaEntity = MemberDutyJpaEntity.builder()
                .member(mockMember)
                .duty(mockDuty)
                .build();

        MemberCleaningJpaEntity memberCleaningJpaEntity = MemberCleaningJpaEntity.builder()
                .member(mockMember)
                .cleaning(mockCleaningJpaEntity)
                .build();

        mockPlace.setTime(LocalTime.of(9, 0), LocalTime.of(18, 0), true);

        Checklist checklist = Checklist.builder()
                .cleaning(mockCleaningJpaEntity)
                .isComplete(false)
                .build();
        ReflectionTestUtils.setField(checklist, "checklistId", 3000L);
        LocalDateTime createdAt = LocalDate.now().atTime(12, 0);
        ReflectionTestUtils.setField(checklist, "createdAt", createdAt);

        given(memberDutyRepository.findAllWithMemberAndPlaceByPlaceId(10L))
                .willReturn(List.of(memberDutyJpaEntity));
        given(memberCleaningRepository.findAllByMember(mockMember))
                .willReturn(List.of(memberCleaningJpaEntity));
        given(checklistRepository.findWithCleaningByDutyId(1000L))
                .willReturn(List.of(checklist));

        // when
        PlaceResult result = placeQueryService.getPlace();

        // then
        assertThat(result.memberId()).isEqualTo(100L);
        assertThat(result.duty()).isNotNull();
    }

    @Test
    @DisplayName("플레이스 조회 - 매니저")
    void getPlace_manager() {
        // given
        MemberContext.set(mockManager);

        MemberDutyJpaEntity memberDutyJpaEntity = MemberDutyJpaEntity.builder()
                .member(mockMember)
                .duty(mockDuty)
                .build();

        MemberCleaningJpaEntity memberCleaningJpaEntity = MemberCleaningJpaEntity.builder()
                .member(mockMember)
                .cleaning(mockCleaningJpaEntity)
                .build();

        mockPlace.setTime(LocalTime.of(9, 0), LocalTime.of(18, 0), true);

        Checklist checklist = Checklist.builder()
                .cleaning(mockCleaningJpaEntity)
                .isComplete(false)
                .build();
        ReflectionTestUtils.setField(checklist, "checklistId", 4000L);
        LocalDateTime createdAt = LocalDate.now().atTime(12, 0);
        ReflectionTestUtils.setField(checklist, "createdAt", createdAt);

        given(dutyRepository.findByPlace_PlaceId(10L)).willReturn(List.of(mockDuty));
        given(memberDutyRepository.findAllWithMemberAndPlaceByPlaceId(10L))
                .willReturn(List.of(memberDutyJpaEntity));
        given(memberCleaningRepository.findAllByMember(mockMember))
                .willReturn(List.of(memberCleaningJpaEntity));
        given(checklistRepository.findWithCleaningByDutyId(1000L))
                .willReturn(List.of(checklist));

        // when
        PlaceResult result = placeQueryService.getPlace();

        // then
        assertThat(result.memberId()).isEqualTo(200L);
        assertThat(result.duty()).isNotNull();
    }

    // ==================== getDutiesProgress 테스트 ====================

    @Test
    @DisplayName("당번 진행 상황 조회")
    void getDutiesProgress() {
        // given
        MemberContext.set(mockMember);
        DutyProgressDto dto = new DutyProgressDto(1000L, "청소 당번", 5L, 3L);

        given(dutyRepository.findDutyProgressByPlaceToday(10L)).willReturn(List.of(dto));

        // when
        DutyProgressResult result = placeQueryService.getDutiesProgress();

        // then
        assertThat(result.dutyProgressDtos()).hasSize(1);
        assertThat(result.dutyProgressDtos().get(0).dutyId()).isEqualTo(1000L);
        assertThat(result.dutyProgressDtos().get(0).totalCleaning()).isEqualTo(5L);
        assertThat(result.dutyProgressDtos().get(0).endCleaning()).isEqualTo(3L);
    }

    // ==================== getTimeAndIsToday 테스트 ====================

    @Test
    @DisplayName("시간 및 오늘 여부 조회")
    void getTimeAndIsToday() {
        // given
        MemberContext.set(mockMember);
        mockPlace.setTime(LocalTime.of(10, 0), LocalTime.of(20, 0), true);

        // when
        PlaceTimeResult result = placeQueryService.getTimeAndIsToday();

        // then
        assertThat(result.startTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.endTime()).isEqualTo(LocalTime.of(20, 0));
        assertThat(result.isToday()).isTrue();
    }

    // ==================== getInviteCode 테스트 ====================

    @Test
    @DisplayName("초대 코드 조회 - 코드가 있는 경우")
    void getInviteCode_exists() {
        // given
        MemberContext.set(mockMember);

        // when
        String result = placeQueryService.getInviteCode();

        // then
        assertThat(result).isEqualTo("abc123");
    }

    @Test
    @DisplayName("초대 코드 조회 - 코드가 없는 경우")
    void getInviteCode_notExists() {
        // given
        Place placeWithoutCode = Place.builder()
                .name("코드 없는 장소")
                .category(CAFE)
                .build();
        MemberJpaEntity memberInPlaceWithoutCode = MemberJpaEntity.builder()
                .place(placeWithoutCode)
                .build();
        MemberContext.set(memberInPlaceWithoutCode);

        // when & then
        assertThatThrownBy(() -> placeQueryService.getInviteCode())
                .isInstanceOf(InviteCodeNotExistsException.class);
    }
}
