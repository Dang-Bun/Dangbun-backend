package com.dangbun.domain.cleaning.application.service;

import com.dangbun.domain.checklist.adapter.out.persistence.SpringDataChecklistRepository;
import com.dangbun.domain.checklist.application.port.in.command.CreateChecklistByDateAndTimeUseCase;
import com.dangbun.domain.cleaning.adapter.in.web.dto.request.PostCleaningCreateRequest;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.PostCleaningResponse;
import com.dangbun.domain.cleaning.application.port.out.CleaningCommandPort;
import com.dangbun.domain.cleaning.application.port.out.CleaningQueryPort;
import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.cleaning.exception.custom.CleaningAlreadyExistsException;
import com.dangbun.domain.cleaning.exception.custom.CleaningNotFoundException;
import com.dangbun.domain.cleaning.exception.custom.DutyNotFoundException;
import com.dangbun.domain.cleaningImage.application.port.in.command.CleaningImageCommandUseCase;
import com.dangbun.domain.cleaningdate.application.port.out.CleaningDateCommandPort;
import com.dangbun.domain.duty.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.duty.domain.DutyIcon;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.membercleaning.application.port.out.MemberCleaningCommandPort;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CleaningCommandServiceTest {

    @InjectMocks
    private CleaningCommandService cleaningCommandService;

    @Mock
    private DutyQueryPort dutyQueryPort;

    @Mock
    private CleaningQueryPort cleaningQueryPort;

    @Mock
    private CleaningCommandPort cleaningCommandPort;

    @Mock
    private MemberQueryPort memberQueryPort;

    @Mock
    private CleaningDateCommandPort cleaningDateCommandPort;

    @Mock
    private MemberCleaningCommandPort memberCleaningCommandPort;

    @Mock
    private CreateChecklistByDateAndTimeUseCase createChecklistByDateAndTimeUseCase;

    @Mock
    private SpringDataChecklistRepository checklistRepository;

    @Mock
    private CleaningImageCommandUseCase cleaningImageCommandUseCase;

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
    void 청소_생성_성공() {
        // given
        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "새 청소",
                10L,
                CleaningRepeatType.DAILY,
                null,
                List.of(LocalDate.now().toString()),
                true,
                List.of("멤버1", "멤버2")
        );

        Duty duty = Duty.withId(new Duty.DutyId(10L), "청소 당번", DutyIcon.BROOM, 1L);
        Cleaning savedCleaning = Cleaning.withId(
                new Cleaning.CleaningId(100L),
                "새 청소",
                CleaningRepeatType.DAILY,
                null,
                10L,
                true,
                1L
        );

        List<Member> members = List.of(
                Member.withId(200L, com.dangbun.domain.member.domain.MemberRole.MEMBER, "멤버1", true, Map.of(), 1L, 1L, null),
                Member.withId(201L, com.dangbun.domain.member.domain.MemberRole.MEMBER, "멤버2", true, Map.of(), 1L, 2L, null)
        );

        given(dutyQueryPort.findById(10L)).willReturn(Optional.of(duty));
        given(cleaningQueryPort.existsByNameAndDutyIdAndPlaceId("새 청소", 10L, 1L)).willReturn(false);
        given(cleaningCommandPort.save(any(Cleaning.class))).willReturn(savedCleaning);
        given(memberQueryPort.findAllByNameIn(anyList())).willReturn(members);

        // when
        PostCleaningResponse response = cleaningCommandService.createCleaning(request);

        // then
        assertThat(response.cleaningId()).isEqualTo(100L);
        then(cleaningCommandPort).should().save(any(Cleaning.class));
        then(memberCleaningCommandPort).should().saveAll(anyList());
        then(cleaningDateCommandPort).should().saveAll(anyList());
    }

    @Test
    void 청소_생성_실패_당번_없음() {
        // given
        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "새 청소",
                999L,
                CleaningRepeatType.DAILY,
                null,
                List.of(LocalDate.now().toString()),
                true,
                null
        );

        given(dutyQueryPort.findById(999L)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cleaningCommandService.createCleaning(request))
                .isInstanceOf(DutyNotFoundException.class);

        then(cleaningCommandPort).should(never()).save(any());
    }

    @Test
    void 청소_생성_실패_이미_존재하는_청소() {
        // given
        PostCleaningCreateRequest request = new PostCleaningCreateRequest(
                "중복 청소",
                10L,
                CleaningRepeatType.DAILY,
                null,
                List.of(LocalDate.now().toString()),
                true,
                null
        );

        Duty duty = Duty.withId(new Duty.DutyId(10L), "청소 당번", DutyIcon.BROOM, 1L);

        given(dutyQueryPort.findById(10L)).willReturn(Optional.of(duty));
        given(cleaningQueryPort.existsByNameAndDutyIdAndPlaceId("중복 청소", 10L, 1L)).willReturn(true);

        // when, then
        assertThatThrownBy(() -> cleaningCommandService.createCleaning(request))
                .isInstanceOf(CleaningAlreadyExistsException.class);

        then(cleaningCommandPort).should(never()).save(any());
    }

    @Test
    void 청소_삭제_성공() {
        // given
        Long cleaningId = 100L;
        Cleaning cleaning = Cleaning.withId(
                new Cleaning.CleaningId(cleaningId),
                "삭제할 청소",
                CleaningRepeatType.DAILY,
                null,
                10L,
                false,
                1L
        );

        given(cleaningQueryPort.findById(cleaningId)).willReturn(Optional.of(cleaning));
        given(checklistRepository.findByCleaningJpaEntity_CleaningId(cleaningId)).willReturn(List.of());

        // when
        cleaningCommandService.deleteCleaning(cleaningId);

        // then
        then(cleaningCommandPort).should().deleteById(cleaningId);
    }

    @Test
    void 청소_삭제_실패_청소_없음() {
        // given
        Long cleaningId = 999L;

        given(cleaningQueryPort.findById(cleaningId)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> cleaningCommandService.deleteCleaning(cleaningId))
                .isInstanceOf(CleaningNotFoundException.class);

        then(cleaningCommandPort).should(never()).deleteById(any());
    }
}
