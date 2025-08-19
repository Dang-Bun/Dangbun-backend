package com.dangbun.domain.cleaning.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.checklist.service.CreateChecklistService;
import com.dangbun.domain.cleaning.dto.request.PostCleaningCreateRequest;
import com.dangbun.domain.cleaning.dto.request.PutCleaningUpdateRequest;
import com.dangbun.domain.cleaning.dto.response.GetCleaningDetailListResponse;
import com.dangbun.domain.cleaning.dto.response.GetCleaningListResponse;
import com.dangbun.domain.cleaning.dto.response.GetCleaningUnassignedResponse;
import com.dangbun.domain.cleaning.dto.response.PostCleaningResponse;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaning.exception.custom.CleaningAlreadyExistsException;
import com.dangbun.domain.cleaning.exception.custom.CleaningNotFoundException;
import com.dangbun.domain.cleaning.exception.custom.DutyNotFoundException;
import com.dangbun.domain.cleaning.exception.custom.InvalidDateFormatException;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import com.dangbun.domain.cleaningImage.entity.CleaningImage;
import com.dangbun.domain.cleaningImage.repository.CleaningImageRepository;
import com.dangbun.domain.cleaningdate.repository.CleaningDateRepository;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.global.context.DutyContext;
import com.dangbun.global.context.MemberContext;
import com.dangbun.global.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.dangbun.domain.cleaning.entity.CleaningRepeatType.*;
import static com.dangbun.domain.place.entity.PlaceCategory.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CleaningServiceTest {
    @InjectMocks
    private CleaningService cleaningService;

    @Mock
    private MemberCleaningRepository memberCleaningRepository;
    @Mock private DutyRepository dutyRepository;
    @Mock private CleaningRepository cleaningRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private CleaningDateRepository cleaningDateRepository;
    @Mock private CreateChecklistService createChecklistService;
    @Mock private ChecklistRepository checklistRepository;
    @Mock private CleaningImageRepository cleaningImageRepository;
    @Mock private S3Service s3Service;

    private Place mockPlace;
    private Member mockMember;
    private Duty mockDuty;

    @BeforeEach
    void setUp() {
        mockPlace = Place.builder().name("투썸플레이스").category(CAFE).build();
        ReflectionTestUtils.setField(mockPlace, "placeId", 1L);

        mockMember = Member.builder().name("철수").place(mockPlace).build();
        ReflectionTestUtils.setField(mockMember, "memberId", 10L);
        MemberContext.set(mockMember);

        mockDuty = Duty.builder().name("사무실 청소 당번").place(mockPlace).build();
        ReflectionTestUtils.setField(mockDuty, "dutyId", 100L);
        DutyContext.set(mockDuty);
    }


    @Test
    @DisplayName("선택 멤버가 참여 중인 청소의 당번 목록 조회 - memberIds 없으면 전체 당번 반환")
    void getCleaningList_allDuties() {
        // given
        Duty duty2 = Duty.builder().name("회의실 청소").place(mockPlace).build();
        ReflectionTestUtils.setField(duty2, "dutyId", 200L);

        given(dutyRepository.findAll()).willReturn(List.of(mockDuty, duty2));

        // when
        List<GetCleaningListResponse> result = cleaningService.getCleaningList(null);

        // then
        assertThat(result).hasSize(2);
        then(dutyRepository).should().findAll();
    }

    @Test
    @DisplayName("선택 멤버가 참여 중인 청소의 당번 목록 조회 - memberIds 지정 시 해당 당번 반환")
    void getCleaningList_withMemberIds() {
        // given
        given(memberCleaningRepository.findDistinctDutiesByMemberIds(anyList()))
                .willReturn(List.of(mockDuty));

        // when
        List<GetCleaningListResponse> result = cleaningService.getCleaningList(List.of(10L));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).dutyId()).isEqualTo(100L);
        then(memberCleaningRepository).should().findDistinctDutiesByMemberIds(anyList());
    }

    @Test
    @DisplayName("특정 당번의 선택 멤버가 참여 중인 청소 목록 조회 - memberIds 없으면 해당 당번의 전체 청소 반환")
    void getCleaningDetailList_allCleanings() {
        // given
        Cleaning cleaning1 = Cleaning.builder().name("바닥 쓸기").duty(mockDuty).place(mockPlace).build();
        Cleaning cleaning2 = Cleaning.builder().name("창문 닦기").duty(mockDuty).place(mockPlace).build();
        ReflectionTestUtils.setField(cleaning1, "cleaningId", 200L);
        ReflectionTestUtils.setField(cleaning2, "cleaningId", 201L);

        given(cleaningRepository.findAllByDuty(mockDuty)).willReturn(List.of(cleaning1, cleaning2));
        given(memberCleaningRepository.findMembersByCleaningId(200L))
                .willReturn(List.of(
                        mockMember,
                        Member.builder().name("영희").build(),
                        Member.builder().name("민수").build()
                ));
        given(memberCleaningRepository.findMembersByCleaningId(201L))
                .willReturn(List.of(
                        Member.builder().name("지윤").build(),
                        Member.builder().name("종혁").build()
                ));

        // when
        List<GetCleaningDetailListResponse> result = cleaningService.getCleaningDetailList(null);

        // then
        assertThat(result).hasSize(2);

        GetCleaningDetailListResponse r1 = result.get(0);
        assertThat(r1.cleaningName()).isEqualTo("바닥 쓸기");
        assertThat(r1.memberCount()).isEqualTo(3);
        assertThat(r1.displayedMemberNames()).containsExactly("철수", "영희");

        GetCleaningDetailListResponse r2 = result.get(1);
        assertThat(r2.cleaningName()).isEqualTo("창문 닦기");
        assertThat(r2.memberCount()).isEqualTo(2);
        assertThat(r2.displayedMemberNames()).containsExactly("지윤", "종혁");
    }

    @Test
    @DisplayName("특정 당번의 선택 멤버가 참여 중인 청소 목록 조회 - memberIds 지정 시 Duty 내 특정 청소만 반환")
    void getCleaningDetailList_withMemberIds() {
        Cleaning cleaning1 = Cleaning.builder().name("창문 닦기").duty(mockDuty).place(mockPlace).build();
        Cleaning cleaning2 = Cleaning.builder().name("쓰레기 버리기").duty(mockDuty).place(mockPlace).build();
        ReflectionTestUtils.setField(cleaning1, "cleaningId", 200L);
        ReflectionTestUtils.setField(cleaning2, "cleaningId", 201L);

        given(cleaningRepository.findByDutyIdAndMemberIdsWithMembersJoin(eq(100L), anyList()))
                .willReturn(List.of(cleaning1, cleaning2));
        given(memberCleaningRepository.findMembersByCleaningId(200L))
                .willReturn(List.of(
                        Member.builder().name("영희").build(),
                        Member.builder().name("철수").build()
                ));
        given(memberCleaningRepository.findMembersByCleaningId(201L))
                .willReturn(List.of(
                        Member.builder().name("민수").build(),
                        Member.builder().name("수지").build(),
                        Member.builder().name("지윤").build()
                ));

        // when
        List<GetCleaningDetailListResponse> result = cleaningService.getCleaningDetailList(List.of(10L));

        // then
        assertThat(result).hasSize(2);

        GetCleaningDetailListResponse r1 = result.get(0);
        assertThat(r1.cleaningName()).isEqualTo("창문 닦기");
        assertThat(r1.memberCount()).isEqualTo(2);
        assertThat(r1.displayedMemberNames()).containsExactly("영희", "철수");

        GetCleaningDetailListResponse r2 = result.get(1);
        assertThat(r2.cleaningName()).isEqualTo("쓰레기 버리기");
        assertThat(r2.memberCount()).isEqualTo(3);
        assertThat(r2.displayedMemberNames()).containsExactly("민수", "수지");
    }

    @Test
    @DisplayName("미지정 청소 목록 조회")
    void getUnassignedCleanings_success() {
        //given
        Cleaning cleaning = Cleaning.builder().name("복도청소").duty(null).place(mockPlace).build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 400L);

        given(cleaningRepository.findUnassignedCleaningsByPlaceId(1L)).willReturn(List.of(cleaning));

        //when
        List<GetCleaningUnassignedResponse> result = cleaningService.getUnassignedCleanings();

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).cleaningName()).isEqualTo("복도청소");
    }


    @Test
    @DisplayName("당번별 청소 생성- 성공")
    void createCleaning_success() {
        // given
        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "거실 청소",
                null,
                null,
                List.of("철수", "영희"),
                false,
                DAILY,
                null,
                List.of("2025-08-21", "2025-08-22")
        );

        given(cleaningRepository.existsByNameAndDutyAndPlace("거실 청소", null, mockPlace))
                .willReturn(false);

        Cleaning cleaning = Cleaning.builder().name("거실 청소").place(mockPlace).build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 200L);

        given(cleaningRepository.save(any(Cleaning.class))).willReturn(cleaning);
        given(memberRepository.findAllByNameIn(anyList()))
                .willReturn(List.of(
                        Member.builder().name("철수").build(),
                        Member.builder().name("영희").build()
                ));

        given(cleaningDateRepository.saveAll(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        PostCleaningResponse response = cleaningService.createCleaning(request);

        // then
        assertThat(response.cleaningId()).isEqualTo(200L);
        then(cleaningRepository).should().save(any(Cleaning.class));
        then(memberRepository).should().findAllByNameIn(anyList());
        then(cleaningDateRepository).should().saveAll(anyList());
        then(createChecklistService).should()
                .createChecklistByDateAndTime(any(Cleaning.class), anyList(), any(Place.class));
    }

    @Test
    @DisplayName("당번별 청소 생성 - 중복 이름 예외 발생")
    void createCleaning_duplicateName() {
        // given
        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "거실 청소",
                null,
                null,
                null,
                false,
                DAILY,
                null,
                List.of("2025-08-21")
        );

        given(cleaningRepository.existsByNameAndDutyAndPlace("거실 청소", null, mockPlace))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> cleaningService.createCleaning(request))
                .isInstanceOf(CleaningAlreadyExistsException.class);
    }

    @Test
    @DisplayName("당번별 청소 생성 - 존재하지 않는 Duty 예외 발생")
    void createCleaning_dutyNotFound() {
        // given
        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "주방 청소",
                999L,
                "없는당번",
                List.of("지윤"),
                true,
                WEEKLY,
                List.of("MONDAY"),
                List.of("2025-08-21")
        );

        given(dutyRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cleaningService.createCleaning(request))
                .isInstanceOf(DutyNotFoundException.class);
    }

    @Test
    @DisplayName("당번별 청소 수정 - 성공")
    void updateCleaning_success() {
        //given
        PutCleaningUpdateRequest request = new PutCleaningUpdateRequest(
                "바닥 쓸기", "사무실 청소 당번", List.of("철수"), false, null, null, List.of("2025-08-21")
        );

        Cleaning cleaning = Cleaning.builder().name("기존 청소").place(mockPlace).build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 200L);

        given(cleaningRepository.findWithDutyNullableById(200L)).willReturn(Optional.of(cleaning));
        given(dutyRepository.findByName("사무실 청소 당번"))
                .willReturn(Optional.of(mockDuty));
        given(cleaningRepository.existsByNameAndDutyAndCleaningIdNotAndPlace(
                "바닥 쓸기", mockDuty, 200L, mockPlace))
                .willReturn(false);
        given(memberRepository.findAllByNameIn(List.of("철수")))
                .willReturn(List.of(mockMember));

        //when
        cleaningService.updateCleaning(200L, request);

        //then
        assertThat(cleaning.getName()).isEqualTo("바닥 쓸기");
        then(cleaningRepository).should().findWithDutyNullableById(200L);
        then(dutyRepository).should().findByName("사무실 청소 당번");
        then(cleaningRepository).should().existsByNameAndDutyAndCleaningIdNotAndPlace(
                "바닥 쓸기", mockDuty, 200L, mockPlace);
        then(memberCleaningRepository).should().deleteAllByCleaning_CleaningId(200L);
        then(cleaningDateRepository).should().deleteAllByCleaning_CleaningId(200L);
        then(cleaningDateRepository).should().saveAll(anyList());
        then(memberRepository).should().findAllByNameIn(List.of("철수"));
    }

    @Test
    @DisplayName("당번별 청소 수정 - 존재하지 않는 Cleaning ID 예외 발생")
    void updateCleaning_notFound() {
        // given
        PutCleaningUpdateRequest request = new PutCleaningUpdateRequest(
                "바닥 쓸기", null, null, false, null, null, List.of("2025-08-21")
        );

        given(cleaningRepository.findWithDutyNullableById(999L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cleaningService.updateCleaning(999L, request))
                .isInstanceOf(CleaningNotFoundException.class);

        then(cleaningRepository).should().findWithDutyNullableById(999L);
        then(cleaningRepository).shouldHaveNoMoreInteractions();
        then(memberRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("청소 수정 - 존재하지 않는 Duty 예외")
    void updateCleaning_dutyNotFound() {
        // given
        PutCleaningUpdateRequest request = new PutCleaningUpdateRequest(
                "바닥 쓸기", "없는 당번", null, false, null, null, List.of("2025-08-21")
        );

        Cleaning cleaning = Cleaning.builder().name("기존 청소").place(mockPlace).build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 200L);

        given(cleaningRepository.findWithDutyNullableById(200L)).willReturn(Optional.of(cleaning));
        given(dutyRepository.findByName("없는 당번")).willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> cleaningService.updateCleaning(200L, request))
                .isInstanceOf(DutyNotFoundException.class);
    }

    @Test
    @DisplayName("청소 수정 - 청소 중복 이름 예외")
    void updateCleaning_duplicateName() {
        // given
        PutCleaningUpdateRequest request = new PutCleaningUpdateRequest(
                "바닥 쓸기", null, null, false, null, null, List.of("2025-08-21")
        );

        Cleaning cleaning = Cleaning.builder().name("기존 청소").place(mockPlace).build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 200L);

        given(cleaningRepository.findWithDutyNullableById(200L)).willReturn(Optional.of(cleaning));
        given(cleaningRepository.existsByNameAndDutyAndCleaningIdNotAndPlace("바닥 쓸기", null, 200L, mockPlace))
                .willReturn(true);

        // when // then
        assertThatThrownBy(() -> cleaningService.updateCleaning(200L, request))
                .isInstanceOf(CleaningAlreadyExistsException.class);
    }

    @Test
    @DisplayName("청소 수정 - 잘못된 날짜 포맷 예외")
    void updateCleaning_invalidDate() {
        // given
        PutCleaningUpdateRequest request = new PutCleaningUpdateRequest(
                "바닥 쓸기", null, null, false, null, null, List.of("잘못된-날짜")
        );

        Cleaning cleaning = Cleaning.builder().name("기존 청소").place(mockPlace).build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 200L);

        given(cleaningRepository.findWithDutyNullableById(200L)).willReturn(Optional.of(cleaning));
        given(cleaningRepository.existsByNameAndDutyAndCleaningIdNotAndPlace("바닥 쓸기", null, 200L, mockPlace))
                .willReturn(false);

        // when // then
        assertThatThrownBy(() -> cleaningService.updateCleaning(200L, request))
                .isInstanceOf(InvalidDateFormatException.class);
    }

    @Test
    @DisplayName("청소 삭제 - 성공")
    void deleteCleaning_success() {
        // given
        Cleaning cleaning = Cleaning.builder().name("회의실 청소").place(mockPlace).build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 200L);

        Checklist checklist = Checklist.builder().cleaning(cleaning).build();
        ReflectionTestUtils.setField(checklist, "checklistId", 500L);

        CleaningImage image = CleaningImage.builder().checklist(checklist).s3Key("test/key.png").build();

        given(cleaningRepository.findById(200L)).willReturn(Optional.of(cleaning));
        given(checklistRepository.findByCleaning_CleaningId(200L)).willReturn(List.of(checklist));
        given(cleaningImageRepository.findByChecklist_ChecklistId(500L)).willReturn(Optional.of(image));

        // when
        cleaningService.deleteCleaning(200L);

        // then
        then(checklistRepository).should().findByCleaning_CleaningId(200L);
        then(cleaningImageRepository).should().findByChecklist_ChecklistId(500L);
        then(s3Service).should().deleteFile("test/key.png");
        then(cleaningRepository).should().delete(cleaning);
    }

    @Test
    @DisplayName("청소 삭제 - 존재하지 않는 Cleaning ID 예외 발생")
    void deleteCleaning_notFound() {
        //given
        given(cleaningRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cleaningService.deleteCleaning(999L))
                .isInstanceOf(CleaningNotFoundException.class);
    }

    @Test
    @DisplayName("청소 삭제 - 체크리스트는 있지만 이미지 없는 경우")
    void deleteCleaning_checklistWithoutImage() {
        // given
        Cleaning cleaning = Cleaning.builder().name("바닥 쓸기").place(mockPlace).build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 200L);

        Checklist checklist = Checklist.builder().cleaning(cleaning).build();
        ReflectionTestUtils.setField(checklist, "checklistId", 500L);

        given(cleaningRepository.findById(200L)).willReturn(Optional.of(cleaning));
        given(checklistRepository.findByCleaning_CleaningId(200L)).willReturn(List.of(checklist));
        given(cleaningImageRepository.findByChecklist_ChecklistId(500L)).willReturn(Optional.empty());

        // when
        cleaningService.deleteCleaning(200L);

        // then
        then(s3Service).should(never()).deleteFile(anyString());
        then(cleaningRepository).should().delete(cleaning);
    }

    @Test
    @DisplayName("청소 삭제 - 체크리스트가 아예 없는 경우")
    void deleteCleaning_noChecklists() {
        // given
        Cleaning cleaning = Cleaning.builder().name("바닥 쓸기").place(mockPlace).build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 200L);

        given(cleaningRepository.findById(200L)).willReturn(Optional.of(cleaning));
        given(checklistRepository.findByCleaning_CleaningId(200L)).willReturn(List.of());

        // when
        cleaningService.deleteCleaning(200L);

        // then
        then(s3Service).should(never()).deleteFile(anyString());
        then(cleaningRepository).should().delete(cleaning);
    }
}


