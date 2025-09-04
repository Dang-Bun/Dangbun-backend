package com.dangbun.domain.place.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.domain.notificationreceiver.repository.NotificationReceiverRepository;
import com.dangbun.domain.place.dto.request.*;
import com.dangbun.domain.place.dto.response.*;
import com.dangbun.domain.place.dto.response.DutyProgressDto;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.exception.custom.*;
import com.dangbun.domain.place.repository.PlaceRepository;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.context.MemberContext;
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
import java.util.*;

import static com.dangbun.domain.place.entity.PlaceCategory.*;
import static com.dangbun.domain.place.dto.response.GetPlaceListResponse.PlaceDto;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @InjectMocks
    private PlaceService placeService;

    @Mock private PlaceRepository placeRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private UserRepository userRepository;
    @Mock private MemberDutyRepository memberDutyRepository;
    @Mock private MemberCleaningRepository memberCleaningRepository;
    @Mock private ChecklistRepository checkListRepository;
    @Mock private DutyRepository dutyRepository;
    @Mock private NotificationReceiverRepository notificationReceiverRepository;
    @Mock private CleaningRepository cleaningRepository;

    private User mockUser;
    private Place mockPlace;
    private Member mockMember;
    private Member mockManager;
    private Duty mockDuty;
    private Cleaning mockCleaning;

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

        mockMember = Member.builder()
                .name("홍길동")
                .place(mockPlace)
                .role(MemberRole.MEMBER)
                .status(true)
                .user(mockUser)
                .information(Map.of("phone", "010-1234-5678"))
                .build();
        ReflectionTestUtils.setField(mockMember, "memberId", 100L);

        mockManager = Member.builder()
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

        mockCleaning = Cleaning.builder()
                .place(mockPlace)
                .name("화장실 청소")
                .build();
        ReflectionTestUtils.setField(mockCleaning, "cleaningId", 2000L);
    }

    @Test
    @DisplayName("사용자의 장소 목록 조회 - 대기 중인 멤버")
    void getPlaces_withWaitingMember() {
        // given
        Member waitingMember = Member.builder()
                .name("대기자")
                .place(mockPlace)
                .role(MemberRole.WAITING)
                .status(false)
                .user(mockUser)
                .build();

        given(memberRepository.findWithPlaceByUserId(1L)).willReturn(List.of(waitingMember));

        // when
        GetPlaceListResponse result = placeService.getPlaces(1L);

        // then
        assertThat(result.places()).hasSize(1);
        PlaceDto placeDto = result.places().get(0);
        assertThat(placeDto.placeId()).isEqualTo(10L);
        assertThat(placeDto.name()).isEqualTo("테스트 카페");
        assertThat(placeDto.category()).isEqualTo(CAFE);
        assertThat(placeDto.totalCleaning()).isNull();
        assertThat(placeDto.endCleaning()).isNull();
    }

    @Test
    @DisplayName("사용자의 장소 목록 조회 - 일반 멤버")
    void getPlaces_withActiveMember() {
        // given
        MemberCleaning memberCleaning = MemberCleaning.builder()
                .member(mockMember)
                .cleaning(mockCleaning)
                .build();

        given(memberRepository.findWithPlaceByUserId(1L)).willReturn(List.of(mockMember));
        given(memberCleaningRepository.findAllByMember(mockMember)).willReturn(List.of(memberCleaning));
        given(checkListRepository.existsCompletedChecklistByDateAndCleaning(any(), any(), any())).willReturn(false);
        given(notificationReceiverRepository.countUnreadByMemberId(100L)).willReturn(3);

        // when
        GetPlaceListResponse result = placeService.getPlaces(1L);

        // then
        assertThat(result.places()).hasSize(1);
        PlaceDto placeDto = result.places().get(0);
        assertThat(placeDto.placeId()).isEqualTo(10L);
        assertThat(placeDto.totalCleaning()).isEqualTo(1);
        assertThat(placeDto.endCleaning()).isEqualTo(0);
        assertThat(placeDto.role()).isEqualTo("멤버");
        assertThat(placeDto.notifyNumber()).isEqualTo(3);
    }

    @Test
    @DisplayName("사용자의 장소 목록 조회 - 매니저")
    void getPlaces_withManager() {
        // given
        given(memberRepository.findWithPlaceByUserId(1L)).willReturn(List.of(mockManager));
        given(cleaningRepository.findByPlace(mockPlace)).willReturn(List.of(mockCleaning));
        given(checkListRepository.existsCompletedChecklistByDateAndCleaning(any(), any(), any())).willReturn(true);
        given(notificationReceiverRepository.countUnreadByMemberId(200L)).willReturn(0);

        // when
        GetPlaceListResponse result = placeService.getPlaces(1L);

        // then
        assertThat(result.places()).hasSize(1);
        PlaceDto placeDto = result.places().get(0);
        assertThat(placeDto.totalCleaning()).isEqualTo(1);
        assertThat(placeDto.endCleaning()).isEqualTo(1);
        assertThat(placeDto.role()).isEqualTo("매니저");
    }

    @Test
    @DisplayName("매니저와 함께 장소 생성 - 카테고리가 ETC가 아닌 경우")
    void createPlaceWithManager_notETC() {
        // given
        PostCreatePlaceRequest request = new PostCreatePlaceRequest(
                "새 카페",
                CAFE,
                null,
                "매니저",
                Map.of("phone", "010-1111-1111")
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
        given(placeRepository.save(any(Place.class))).willAnswer(invocation -> {
            Place place = invocation.getArgument(0);
            ReflectionTestUtils.setField(place, "placeId", 20L);
            return place;
        });
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        PostCreatePlaceResponse result = placeService.createPlaceWithManager(1L, request);

        // then
        assertThat(result.placeId()).isEqualTo(20L);
        then(placeRepository).should().save(argThat(place ->
                place.getName().equals("새 카페") &&
                        place.getCategory() == CAFE &&
                        place.getCategoryName().equals("카페")
        ));
        then(memberRepository).should().save(argThat(member ->
                member.getName().equals("매니저") &&
                        member.getRole() == MemberRole.MANAGER &&
                        member.getStatus()
        ));
    }

    @Test
    @DisplayName("매니저와 함께 장소 생성 - 카테고리가 ETC인 경우")
    void createPlaceWithManager_withETC() {
        // given
        PostCreatePlaceRequest request = new PostCreatePlaceRequest(
                "기타 장소",
                ETC,
                "커스텀 카테고리",
                "매니저",
                Map.of("address", "서울시")
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
        given(placeRepository.save(any(Place.class))).willAnswer(invocation -> {
            Place place = invocation.getArgument(0);
            ReflectionTestUtils.setField(place, "placeId", 30L);
            return place;
        });

        // when
        PostCreatePlaceResponse result = placeService.createPlaceWithManager(1L, request);

        // then
        assertThat(result.placeId()).isEqualTo(30L);
        then(placeRepository).should().save(argThat(place ->
                place.getCategoryName().equals("커스텀 카테고리")
        ));
    }

    @Test
    @DisplayName("매니저와 함께 장소 생성 - 사용자가 없는 경우")
    void createPlaceWithManager_userNotFound() {
        // given
        PostCreatePlaceRequest request = new PostCreatePlaceRequest(
                "새 장소", CAFE, null, "매니저", Map.of()
        );
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> placeService.createPlaceWithManager(1L, request))
                .isInstanceOf(NoSuchUserException.class);
    }

    @Test
    @DisplayName("초대 코드 생성")
    void createInviteCode() {
        // given
        MemberContext.set(mockMember);

        // when
        PostCreateInviteCodeResponse result = placeService.createInviteCode();

        // then
        assertThat(result.inviteCode()).isNotNull();
        assertThat(result.inviteCode()).hasSize(6);
    }

    @Test
    @DisplayName("초대 코드 확인 - 유효한 코드")
    void checkInviteCode_valid() {
        // given
        PostCheckInviteCodeRequest request = new PostCheckInviteCodeRequest("abc123");

        given(placeRepository.findByInviteCode("abc123")).willReturn(mockPlace);
        given(memberRepository.findByPlaceAndUser(mockPlace, mockUser)).willReturn(Optional.empty());
        given(memberRepository.findFirstByPlace(mockPlace)).willReturn(mockMember);

        // when
        PostCheckInviteCodeResponse result = placeService.checkInviteCode(mockUser, request);

        // then
        assertThat(result.placeId()).isEqualTo(10L);
        assertThat(result.information()).contains("phone");
    }

    @Test
    @DisplayName("초대 코드 확인 - 유효하지 않은 코드")
    void checkInviteCode_invalid() {
        // given
        PostCheckInviteCodeRequest request = new PostCheckInviteCodeRequest("wrong");
        given(placeRepository.findByInviteCode("wrong")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> placeService.checkInviteCode(mockUser, request))
                .isInstanceOf(InvalidInviteCodeException.class);
    }

    @Test
    @DisplayName("초대 코드 확인 - 이미 초대된 사용자")
    void checkInviteCode_alreadyInvited() {
        // given
        PostCheckInviteCodeRequest request = new PostCheckInviteCodeRequest("abc123");

        given(placeRepository.findByInviteCode("abc123")).willReturn(mockPlace);
        given(memberRepository.findByPlaceAndUser(mockPlace, mockUser)).willReturn(Optional.of(mockMember));

        // when & then
        assertThatThrownBy(() -> placeService.checkInviteCode(mockUser, request))
                .isInstanceOf(AlreadyInvitedException.class);
    }

    @Test
    @DisplayName("장소 가입 요청")
    void joinRequest_success() {
        // given
        PostRegisterPlaceRequest request = new PostRegisterPlaceRequest(
                "abc123",
                "신규멤버",
                Map.of("phone", "010-2222-2222")
        );

        given(memberRepository.findWithPlaceByInviteCode("abc123")).willReturn(List.of(mockMember));
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        PostRegisterPlaceResponse result = placeService.joinRequest(mockUser, request);

        // then
        assertThat(result.placeId()).isEqualTo(10L);
        then(memberRepository).should().save(argThat(member ->
                member.getName().equals("신규멤버") &&
                        member.getRole() == MemberRole.WAITING &&
                        !member.getStatus()
        ));
    }

    @Test
    @DisplayName("장소 가입 요청 - 유효하지 않은 초대 코드")
    void joinRequest_invalidCode() {
        // given
        PostRegisterPlaceRequest request = new PostRegisterPlaceRequest(
                "wrong", "신규멤버", Map.of()
        );

        given(memberRepository.findWithPlaceByInviteCode("wrong")).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> placeService.joinRequest(mockUser, request))
                .isInstanceOf(InvalidInviteCodeException.class);
    }

    @Test
    @DisplayName("장소 가입 요청 - 정보 불일치")
    void joinRequest_invalidInformation() {
        // given
        PostRegisterPlaceRequest request = new PostRegisterPlaceRequest(
                "abc123",
                "신규멤버",
                Map.of("address", "서울시")  // 다른 키
        );

        given(memberRepository.findWithPlaceByInviteCode("abc123")).willReturn(List.of(mockMember));

        // when & then
        assertThatThrownBy(() -> placeService.joinRequest(mockUser, request))
                .isInstanceOf(InvalidInformationException.class);
    }

    @Test
    @DisplayName("장소 정보 조회 - 대기 중인 멤버")
    void getPlace_waitingMember() {
        // given
        Member waitingMember = Member.builder()
                .name("대기자")
                .place(mockPlace)
                .role(MemberRole.WAITING)
                .status(false)
                .build();
        ReflectionTestUtils.setField(waitingMember, "memberId", 300L);
        MemberContext.set(waitingMember);

        // when
        GetPlaceResponse result = placeService.getPlace();

        // then
        assertThat(result.memberId()).isEqualTo(300L);
        assertThat(result.placeId()).isEqualTo(10L);
        assertThat(result.placeName()).isEqualTo("테스트 카페");
        assertThat(result.duties()).isNull();
    }

    @Test
    @DisplayName("장소 정보 조회 - 일반 멤버")
    void getPlace_activeMember() {
        // given
        MemberContext.set(mockMember);

        MemberDuty memberDuty = MemberDuty.builder()
                .member(mockMember)
                .duty(mockDuty)
                .build();

        MemberCleaning memberCleaning = MemberCleaning.builder()
                .member(mockMember)
                .cleaning(mockCleaning)
                .build();

        Checklist checklist = Checklist.builder()
                .cleaning(mockCleaning)
                .isComplete(false)
                .build();
        ReflectionTestUtils.setField(checklist, "checklistId", 3000L);
        ReflectionTestUtils.setField(checklist, "createdAt", LocalDateTime.now());

        given(memberDutyRepository.findAllWithMemberAndPlaceByPlaceId(10L))
                .willReturn(List.of(memberDuty));
        given(memberCleaningRepository.findAllByMember(mockMember))
                .willReturn(List.of(memberCleaning));
        given(checkListRepository.findWithCleaningByDutyId(1000L))
                .willReturn(List.of(checklist));

        mockPlace.setTime(LocalTime.of(9, 0), LocalTime.of(18, 0), true);

        // when
        GetPlaceResponse result = placeService.getPlace();

        // then
        assertThat(result.memberId()).isEqualTo(100L);
        assertThat(result.duties()).isNotNull();
        assertThat(result.duties()).hasSize(1);
    }

    @Test
    @DisplayName("장소 정보 조회 - 매니저")
    void getPlace_manager() {
        // given
        MemberContext.set(mockManager);

        MemberDuty memberDuty = MemberDuty.builder()
                .member(mockMember)
                .duty(mockDuty)
                .build();

        MemberCleaning memberCleaning = MemberCleaning.builder()
                .member(mockMember)
                .cleaning(mockCleaning)
                .build();

        Checklist checklist = Checklist.builder()
                .cleaning(mockCleaning)
                .isComplete(false)
                .build();
        ReflectionTestUtils.setField(checklist, "checklistId", 4000L);
        ReflectionTestUtils.setField(checklist, "createdAt", LocalDateTime.now());

        given(dutyRepository.findByPlace_PlaceId(10L)).willReturn(List.of(mockDuty));
        given(memberDutyRepository.findAllWithMemberAndPlaceByPlaceId(10L))
                .willReturn(List.of(memberDuty));
        given(memberCleaningRepository.findAllByMember(mockMember))
                .willReturn(List.of(memberCleaning));
        given(checkListRepository.findWithCleaningByDutyId(1000L))
                .willReturn(List.of(checklist));

        mockPlace.setTime(LocalTime.of(9, 0), LocalTime.of(18, 0), true);

        // when
        GetPlaceResponse result = placeService.getPlace();

        // then
        assertThat(result.memberId()).isEqualTo(200L);
        assertThat(result.duties()).isNotNull();
    }

    @Test
    @DisplayName("장소 삭제 - 성공")
    void deletePlace_success() {
        // given
        MemberContext.set(mockMember);
        DeletePlaceRequest request = new DeletePlaceRequest("테스트 카페");

        // when
        placeService.deletePlace(request);

        // then
        then(placeRepository).should().delete(mockPlace);
    }

    @Test
    @DisplayName("장소 삭제 - 이름 불일치")
    void deletePlace_invalidName() {
        // given
        MemberContext.set(mockMember);
        DeletePlaceRequest request = new DeletePlaceRequest("잘못된 이름");

        // when & then
        assertThatThrownBy(() -> placeService.deletePlace(request))
                .isInstanceOf(InvalidPlaceNameException.class);
    }

    @Test
    @DisplayName("등록 취소")
    void cancelRegister() {
        // given
        MemberContext.set(mockMember);

        // when
        placeService.cancelRegister();

        // then
        then(memberRepository).should().delete(mockMember);
    }

    @Test
    @DisplayName("시간 업데이트 - 오늘 날짜이고 유효한 시간")
    void updateTime_todayValidTime() {
        // given
        MemberContext.set(mockMember);
        PatchUpdateTimeRequest request = new PatchUpdateTimeRequest(
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                true
        );

        // when
        PatchUpdateTimeResponse result = placeService.updateTime(request);

        // then
        assertThat(result.startTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.endTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(result.isToday()).isTrue();
    }

    @Test
    @DisplayName("시간 업데이트 - 오늘 날짜이고 시작시간이 종료시간보다 늦음")
    void updateTime_todayInvalidTime() {
        // given
        MemberContext.set(mockMember);
        PatchUpdateTimeRequest request = new PatchUpdateTimeRequest(
                LocalTime.of(18, 0),
                LocalTime.of(9, 0),
                true
        );

        // when & then
        assertThatThrownBy(() -> placeService.updateTime(request))
                .isInstanceOf(InvalidTimeException.class);
    }

    @Test
    @DisplayName("시간 업데이트 - 다음날로 넘어가는 경우")
    void updateTime_nextDay() {
        // given
        MemberContext.set(mockMember);
        PatchUpdateTimeRequest request = new PatchUpdateTimeRequest(
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                false
        );

        // when
        PatchUpdateTimeResponse result = placeService.updateTime(request);

        // then
        assertThat(result.startTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(result.endTime()).isEqualTo(LocalTime.of(6, 0));
        assertThat(result.isToday()).isFalse();
    }

    @Test
    @DisplayName("당번 진행 상황 조회")
    void getDutiesProgress() {
        // given
        MemberContext.set(mockMember);
        DutyProgressDto dto = new DutyProgressDto(
                1000L, "청소 당번", 5L, 3L
        );

        given(dutyRepository.findDutyProgressByPlaceToday(10L)).willReturn(List.of(dto));

        // when
        GetDutiesProgressResponse result = placeService.getDutiesProgress();

        // then
        assertThat(result.dutyProgressDtos()).hasSize(1);
        assertThat(result.dutyProgressDtos().get(0).dutyId()).isEqualTo(1000L);
        assertThat(result.dutyProgressDtos().get(0).totalCleaning()).isEqualTo(5L);
        assertThat(result.dutyProgressDtos().get(0).endCleaning()).isEqualTo(3L);
    }

    @Test
    @DisplayName("시간 및 오늘 여부 조회")
    void getTimeAndIsToday() {
        // given
        MemberContext.set(mockMember);
        mockPlace.setTime(LocalTime.of(10, 0), LocalTime.of(20, 0), true);

        // when
        GetTimeResponse result = placeService.getTimeAndIsToday();

        // then
        assertThat(result.startTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(result.endTime()).isEqualTo(LocalTime.of(20, 0));
        assertThat(result.isToday()).isTrue();
    }

    @Test
    @DisplayName("초대 코드 조회 - 코드가 있는 경우")
    void getInviteCode_exists() {
        // given
        MemberContext.set(mockMember);

        // when
        GetPlaceInvitedCodeResponse result = placeService.getInviteCode();

        // then
        assertThat(result.inviteCode()).isEqualTo("abc123");
    }

    @Test
    @DisplayName("초대 코드 조회 - 코드가 없는 경우")
    void getInviteCode_notExists() {
        // given
        Place placeWithoutCode = Place.builder()
                .name("코드 없는 장소")
                .category(CAFE)
                .build();
        Member memberInPlaceWithoutCode = Member.builder()
                .place(placeWithoutCode)
                .build();
        MemberContext.set(memberInPlaceWithoutCode);

        // when & then
        assertThatThrownBy(() -> placeService.getInviteCode())
                .isInstanceOf(InviteCodeNotExistsException.class);
    }
}
