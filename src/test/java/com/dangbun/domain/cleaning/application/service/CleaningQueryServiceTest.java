package com.dangbun.domain.cleaning.application.service;

import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningListResponse;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningUnassignedResponse;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningMapper;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningRepository;
import com.dangbun.domain.cleaning.application.port.out.CleaningQueryPort;
import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.duty.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.duty.domain.DutyIcon;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.membercleaning.application.port.out.MemberCleaningQueryPort;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CleaningQueryServiceTest {

    @InjectMocks
    private CleaningQueryService cleaningQueryService;

    @Mock
    private DutyQueryPort dutyQueryPort;

    @Mock
    private CleaningQueryPort cleaningQueryPort;

    @Mock
    private MemberCleaningQueryPort memberCleaningQueryPort;

    @Mock
    private CleaningRepository cleaningRepository;

    @Mock
    private CleaningMapper cleaningMapper;

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
    void 청소_목록_조회_전체() {
        // given
        List<Duty> duties = List.of(
                Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.BROOM, 1L),
                Duty.withId(new Duty.DutyId(2L), "설거지 당번", DutyIcon.DISH, 1L)
        );

        given(dutyQueryPort.findAll()).willReturn(duties);

        // when
        List<GetCleaningListResponse> result = cleaningQueryService.getCleaningList(null);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 청소_목록_조회_멤버_필터() {
        // given
        List<Long> memberIds = List.of(100L, 101L);
        List<Duty> duties = List.of(
                Duty.withId(new Duty.DutyId(1L), "청소 당번", DutyIcon.BROOM, 1L)
        );

        given(dutyQueryPort.findDistinctDutiesByMemberIds(memberIds)).willReturn(duties);

        // when
        List<GetCleaningListResponse> result = cleaningQueryService.getCleaningList(memberIds);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void 미지정_청소_목록_조회() {
        // given
        Long placeId = 1L;
        List<Cleaning> unassignedCleanings = List.of(
                Cleaning.withId(new Cleaning.CleaningId(1L), "미지정 청소1", CleaningRepeatType.DAILY, null, null, false, placeId),
                Cleaning.withId(new Cleaning.CleaningId(2L), "미지정 청소2", CleaningRepeatType.WEEKLY, "MON,WED", null, true, placeId)
        );

        given(cleaningQueryPort.findUnassignedCleaningsByPlaceId(placeId)).willReturn(unassignedCleanings);

        // when
        List<GetCleaningUnassignedResponse> result = cleaningQueryService.getUnassignedCleanings();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(GetCleaningUnassignedResponse::cleaningId).containsExactly(1L, 2L);
        assertThat(result).extracting(GetCleaningUnassignedResponse::cleaningName).containsExactly("미지정 청소1", "미지정 청소2");
    }

    @Test
    void 플레이스별_청소_조회() {
        // given
        Long placeId = 1L;
        List<Cleaning> cleanings = List.of(
                Cleaning.withId(new Cleaning.CleaningId(1L), "청소1", CleaningRepeatType.DAILY, null, 10L, false, placeId),
                Cleaning.withId(new Cleaning.CleaningId(2L), "청소2", CleaningRepeatType.ONCE, null, 10L, true, placeId)
        );

        given(cleaningQueryPort.findByPlaceId(placeId)).willReturn(cleanings);

        // when
        List<Cleaning> result = cleaningQueryService.getCleaningsByPlaceId(placeId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(c -> c.getCleaningId().value()).containsExactly(1L, 2L);
    }
}
