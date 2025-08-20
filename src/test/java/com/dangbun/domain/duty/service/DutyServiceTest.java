package com.dangbun.domain.duty.service;

import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import com.dangbun.domain.duty.dto.request.*;
import com.dangbun.domain.duty.dto.response.*;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.exception.custom.*;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.global.context.DutyContext;
import com.dangbun.global.context.MemberContext;
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

import static com.dangbun.domain.duty.entity.DutyAssignType.*;
import static com.dangbun.domain.duty.entity.DutyIcon.*;
import static com.dangbun.domain.place.entity.PlaceCategory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class DutyServiceTest {

    @Mock private DutyRepository dutyRepository;
    @Mock private MemberDutyRepository memberDutyRepository;
    @Mock private CleaningRepository cleaningRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private MemberCleaningRepository memberCleaningRepository;

    @InjectMocks
    private DutyService dutyService;

    @BeforeEach
    void setUp() {
        Place place = Place.builder()
                .name("우리집")
                .category(CAFE)
                .build();
        ReflectionTestUtils.setField(place, "placeId", 1L);

        Member fakeMember = Member.builder()
                .name("철수")
                .place(place)
                .build();
        ReflectionTestUtils.setField(fakeMember, "memberId", 1L);

        MemberContext.set(fakeMember);
    }

    @Test
    @DisplayName("당번 생성 - 성공")
    void createDuty_success() {
        // given
        PostDutyCreateRequest request = new PostDutyCreateRequest("탕비실 청소 당번", BRUSH_PINK);

        given(dutyRepository.existsByNameAndPlace_PlaceId(request.name(), 1L)).willReturn(false);

        Duty duty = Duty.builder()
                .name(request.name())
                .icon(request.icon())
                .place(MemberContext.get().getPlace())
                .build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);

        given(dutyRepository.save(any(Duty.class))).willReturn(duty);

        // when
        PostDutyCreateResponse response = dutyService.createDuty(request);

        // then
        assertThat(response.dutyId()).isEqualTo(1L);
        then(dutyRepository).should().save(any(Duty.class));
    }

    @Test
    @DisplayName("당번 생성 - 이미 있는 당번이면 예외 발생")
    void createDuty_DutyAlreadyExists() {
        // given
        PostDutyCreateRequest request = new PostDutyCreateRequest("탕비실 청소 당번", TRASH_BLUE);
        when(dutyRepository.existsByNameAndPlace_PlaceId("탕비실 청소 당번", 1L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> dutyService.createDuty(request))
                .isInstanceOf(DutyAlreadyExistsException.class);
    }

    @Test
    @DisplayName("당번 목록 조회 - 성공")
    void getDutyList_success() {
        // given
        Duty duty1 = Duty.builder().name("탕비실").icon(DISH_BLUE).place(MemberContext.get().getPlace()).build();
        Duty duty2 = Duty.builder().name("회의실").icon(TRASH_BLUE).place(MemberContext.get().getPlace()).build();
        ReflectionTestUtils.setField(duty1, "dutyId", 1L);
        ReflectionTestUtils.setField(duty2, "dutyId", 2L);

        given(dutyRepository.findByPlace_PlaceId(1L)).willReturn(List.of(duty1, duty2));

        // when
        List<GetDutyListResponse> result = dutyService.getDutyList();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("탕비실", "회의실");
    }

    @Test
    @DisplayName("당번 업데이트 - 성공")
    void updateDuty_success() {
        // given
        Duty duty = Duty.builder()
                .name("탕비실 청소 당번")
                .icon(TRASH_BLUE)
                .place(MemberContext.get().getPlace())
                .build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);

        DutyContext.set(duty);

        PutDutyUpdateRequest request = new PutDutyUpdateRequest("당번 새이름", DISH_BLUE);

        // when
        PutDutyUpdateResponse response = dutyService.updateDuty(request);

        // then
        assertThat(duty.getName()).isEqualTo("당번 새이름");
        assertThat(duty.getIcon()).isEqualTo(DISH_BLUE);

        assertThat(response.dutyId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("당번 새이름");
        assertThat(response.icon()).isEqualTo(DISH_BLUE);
    }

    @Test
    @DisplayName("getDutyMemberNameList - 성공")
    void getDutyMemberNameList_success() {
        Duty duty = Duty.builder().name("탕비실").build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Member member1 = Member.builder().name("철수").build();
        Member member2 = Member.builder().name("영희").build();

        MemberDuty md1 = MemberDuty.builder().duty(duty).member(member1).build();
        MemberDuty md2 = MemberDuty.builder().duty(duty).member(member2).build();

        given(memberDutyRepository.findAllByDuty(duty)).willReturn(List.of(md1, md2));

        List<GetDutyMemberNameListResponse> result = dutyService.getDutyMemberNameList();

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("철수", "영희");
    }

    @Test
    @DisplayName("getDutyCleaningNameList - 성공")
    void getDutyCleaningNameList_success() {
        Duty duty = Duty.builder().name("탕비실").build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Cleaning cleaning1 = Cleaning.builder().name("책상 닦기").duty(duty).build();
        Cleaning cleaning2 = Cleaning.builder().name("바닥 청소").duty(duty).build();

        given(cleaningRepository.findAllByDuty(duty)).willReturn(List.of(cleaning1, cleaning2));

        List<GetDutyCleaningNameListResponse> result = dutyService.getDutyCleaningNameList();

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("책상 닦기", "바닥 청소");
    }


    @Test
    @DisplayName("당번 삭제 - 성공")
    void deleteDuty_success() {
        // given
        Duty duty = Duty.builder()
                .name("탕비실 청소 당번")
                .icon(DISH_BLUE)
                .place(MemberContext.get().getPlace())
                .build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);

        DutyContext.set(duty);

        // when
        dutyService.deleteDuty();

        // then
        then(dutyRepository).should().delete(duty);
    }

    @Test
    @DisplayName("당번의 멤버 수정 - 성공")
    void putMembers_success() {
        // given
        Duty duty = Duty.builder()
                .name("탕비실 청소 당번")
                .icon(DISH_BLUE)
                .place(MemberContext.get().getPlace())
                .build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Member member1 = Member.builder().name("철수").place(MemberContext.get().getPlace()).build();
        ReflectionTestUtils.setField(member1, "memberId", 100L);

        Member member2 = Member.builder().name("영희").place(MemberContext.get().getPlace()).build();
        ReflectionTestUtils.setField(member2, "memberId", 200L);

        given(memberRepository.findAllById(List.of(100L, 200L)))
                .willReturn(List.of(member1, member2));

        PutAddMembersRequest request = new PutAddMembersRequest(List.of(100L, 200L));

        // when
        PutAddMembersResponse response = dutyService.putMembers(request);

        // then
        assertThat(response.addedMemberIds()).containsExactlyInAnyOrder(100L, 200L);
        then(memberDutyRepository).should().deleteAllByDuty(duty);
        then(memberDutyRepository).should(times(2)).save(any(MemberDuty.class));
    }

    @Test
    @DisplayName("당번의 멤버 수정 - 존재하지 않는 멤버 ID 포함 시 예외 발생")
    void putMembers_memberNotFound() {
        // given
        Duty duty = Duty.builder().name("탕비실").icon(DISH_BLUE).place(MemberContext.get().getPlace()).build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Member member1 = Member.builder().name("철수").place(MemberContext.get().getPlace()).build();
        ReflectionTestUtils.setField(member1, "memberId", 100L);


        given(memberRepository.findAllById(List.of(100L, 200L))).willReturn(List.of(member1));

        PutAddMembersRequest request = new PutAddMembersRequest(List.of(100L, 200L));

        // when & then
        assertThatThrownBy(() -> dutyService.putMembers(request))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("당번 역할 분담 - CUSTOM 성공")
    void assignMember_custom_success() {
        // given
        Duty duty = Duty.builder().name("탕비실").build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Cleaning cleaning = Cleaning.builder().name("책상 닦기").duty(duty).build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 10L);

        Member member1 = Member.builder().name("철수").build();
        ReflectionTestUtils.setField(member1, "memberId", 100L);

        Member member2 = Member.builder().name("영희").build();
        ReflectionTestUtils.setField(member2, "memberId", 200L);

        given(cleaningRepository.findByCleaningIdAndDuty_DutyId(10L, 1L))
                .willReturn(Optional.of(cleaning));
        given(memberRepository.findAllById(List.of(100L, 200L)))
                .willReturn(List.of(member1, member2));

        PatchAssignMemberRequest request = new PatchAssignMemberRequest(CUSTOM, 10L, List.of(100L, 200L), null);

        // when
        dutyService.assignMember(request);

        // then
        then(memberCleaningRepository).should().deleteAllByCleaning_CleaningId(10L);
        then(memberCleaningRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("당번 역할 분담 - CUSTOM 실패 (해당하는 청소 없음)")
    void assignMember_custom_cleaningNotFound() {
        // given
        Duty duty = Duty.builder().name("탕비실").build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        given(cleaningRepository.findByCleaningIdAndDuty_DutyId(10L, 1L))
                .willReturn(Optional.empty());

        PatchAssignMemberRequest request = new PatchAssignMemberRequest(CUSTOM, 10L, List.of(100L), null);

        // when & then
        assertThatThrownBy(() -> dutyService.assignMember(request))
                .isInstanceOf(CleaningNotFoundException.class);
    }

    @Test
    @DisplayName("당번 역할 분담 - COMMON 성공")
    void assignMember_common_success() {
        // given
        Duty duty = Duty.builder().name("탕비실").build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Cleaning cleaning1 = Cleaning.builder().name("책상 닦기").duty(duty).build();
        ReflectionTestUtils.setField(cleaning1, "cleaningId", 10L);
        Cleaning cleaning2 = Cleaning.builder().name("바닥 청소").duty(duty).build();
        ReflectionTestUtils.setField(cleaning2, "cleaningId", 20L);

        Member member1 = Member.builder().name("철수").build();
        ReflectionTestUtils.setField(member1, "memberId", 100L);
        Member member2 = Member.builder().name("영희").build();
        ReflectionTestUtils.setField(member2, "memberId", 200L);

        given(cleaningRepository.findAllByDuty(duty))
                .willReturn(List.of(cleaning1, cleaning2));
        given(memberDutyRepository.findMembersByDuty(duty))
                .willReturn(List.of(member1, member2));

        PatchAssignMemberRequest request = new PatchAssignMemberRequest(COMMON, null, null, null);

        // when
        dutyService.assignMember(request);

        // then
        then(memberCleaningRepository).should(times(2)).deleteAllByCleaning_CleaningId(anyLong());
        then(memberCleaningRepository).should(times(2)).saveAll(anyList());
    }

    @Test
    @DisplayName("assignMember - COMMON 실패 (당번에 멤버 없음)")
    void assignMember_common_noMembers() {
        // given
        Duty duty = Duty.builder().name("탕비실").build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Cleaning cleaning = Cleaning.builder().name("책상 닦기").duty(duty).build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 10L);

        given(cleaningRepository.findAllByDuty(duty)).willReturn(List.of(cleaning));
        given(memberDutyRepository.findMembersByDuty(duty)).willReturn(List.of());

        PatchAssignMemberRequest request = new PatchAssignMemberRequest(COMMON, null, null, null);

        // when & then
        assertThatThrownBy(() -> dutyService.assignMember(request))
                .isInstanceOf(MemberNotExistsException.class);
    }

    @Test
    @DisplayName("당번 역할 분담 - RANDOM 성공")
    void assignMember_random_success() {
        // given
        Duty duty = Duty.builder().name("탕비실").build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Cleaning cleaning1 = Cleaning.builder().name("책상 닦기").duty(duty).build();
        ReflectionTestUtils.setField(cleaning1, "cleaningId", 10L);

        Member member1 = Member.builder().name("철수").build();
        ReflectionTestUtils.setField(member1, "memberId", 100L);
        Member member2 = Member.builder().name("영희").build();
        ReflectionTestUtils.setField(member2, "memberId", 200L);
        Member member3 = Member.builder().name("민수").build();
        ReflectionTestUtils.setField(member3, "memberId", 300L);

        given(cleaningRepository.findAllByDuty(duty)).willReturn(List.of(cleaning1));
        given(memberDutyRepository.findMembersByDuty(duty)).willReturn(List.of(member1, member2, member3));

        PatchAssignMemberRequest request = new PatchAssignMemberRequest(RANDOM, null, null, 2);

        // when
        dutyService.assignMember(request);

        // then
        then(memberCleaningRepository).should().deleteAllByCleaning_CleaningId(10L);
        then(memberCleaningRepository).should().saveAll(anyList());
    }


    @Test
    @DisplayName("청소 목록 조회 (상세 정보 포함) - 성공")
    void getCleaningInfoList_success() {
        // given
        Duty duty = Duty.builder().name("탕비실").build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Cleaning cleaning = Cleaning.builder().name("책상 닦기").duty(duty).build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 10L);

        Member member1 = Member.builder().name("철수").build();
        Member member2 = Member.builder().name("영희").build();
        Member member3 = Member.builder().name("민수").build();

        MemberCleaning mc1 = MemberCleaning.builder().cleaning(cleaning).member(member1).build();
        MemberCleaning mc2 = MemberCleaning.builder().cleaning(cleaning).member(member2).build();
        MemberCleaning mc3 = MemberCleaning.builder().cleaning(cleaning).member(member3).build();

        given(cleaningRepository.findAllByDuty(duty)).willReturn(List.of(cleaning));
        given(memberCleaningRepository.findAllByCleaning(cleaning)).willReturn(List.of(mc1, mc2, mc3));

        // when
        List<GetCleaningInfoListResponse> result = dutyService.getCleaningInfoList();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).displayedNames()).containsExactly("철수", "영희");
        assertThat(result.get(0).memberCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("당번에 미지정 청소 추가 - 성공")
    void addCleanings_success() {
        // given
        Duty duty = Duty.builder()
                .name("탕비실 청소 당번")
                .icon(DISH_BLUE)
                .place(MemberContext.get().getPlace())
                .build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Cleaning cleaning1 = Cleaning.builder()
                .name("책상 닦기")
                .build();
        ReflectionTestUtils.setField(cleaning1, "cleaningId", 100L);

        Cleaning cleaning2 = Cleaning.builder()
                .name("바닥 청소")
                .build();
        ReflectionTestUtils.setField(cleaning2, "cleaningId", 200L);

        given(cleaningRepository.findAllById(List.of(100L, 200L)))
                .willReturn(List.of(cleaning1, cleaning2));

        PostAddCleaningsRequest request = new PostAddCleaningsRequest(List.of(100L, 200L));

        // when
        PostAddCleaningsResponse response = dutyService.addCleanings(request);

        // then
        assertThat(response.addedCleaningId()).containsExactlyInAnyOrder(100L, 200L);
        assertThat(cleaning1.getDuty()).isEqualTo(duty);
        assertThat(cleaning2.getDuty()).isEqualTo(duty);
        then(cleaningRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("당번에 미지정 청소 추가 - 일부는 이미 다른 Duty에 속해있는 경우")
    void addCleanings_partialAssigned() {
        // given
        Duty duty = Duty.builder()
                .name("탕비실 청소 당번")
                .icon(DISH_BLUE)
                .place(MemberContext.get().getPlace())
                .build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Duty anotherDuty = Duty.builder()
                .name("회의실 청소 당번")
                .icon(TRASH_BLUE)
                .place(MemberContext.get().getPlace())
                .build();
        ReflectionTestUtils.setField(anotherDuty, "dutyId", 2L);

        Cleaning cleaning1 = Cleaning.builder()
                .name("책상 닦기")
                .duty(anotherDuty)
                .build();
        ReflectionTestUtils.setField(cleaning1, "cleaningId", 100L);

        Cleaning cleaning2 = Cleaning.builder()
                .name("바닥 청소")
                .build();
        ReflectionTestUtils.setField(cleaning2, "cleaningId", 200L);

        given(cleaningRepository.findAllById(List.of(100L, 200L)))
                .willReturn(List.of(cleaning1, cleaning2));

        PostAddCleaningsRequest request = new PostAddCleaningsRequest(List.of(100L, 200L));

        // when
        PostAddCleaningsResponse response = dutyService.addCleanings(request);

        // then
        assertThat(response.addedCleaningId()).containsExactly(200L);
        assertThat(cleaning1.getDuty()).isEqualTo(anotherDuty);
        assertThat(cleaning2.getDuty()).isEqualTo(duty);
        verify(cleaningRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("당번에 미지정 청소 추가 - 일부 Cleaning ID만 존재")
    void addCleanings_partialCleaningNotFound() {
        // given
        Duty duty = Duty.builder()
                .name("탕비실 청소 당번")
                .icon(DISH_BLUE)
                .place(MemberContext.get().getPlace())
                .build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Cleaning cleaning1 = Cleaning.builder()
                .name("책상 닦기")
                .build();
        ReflectionTestUtils.setField(cleaning1, "cleaningId", 100L);

        given(cleaningRepository.findAllById(List.of(100L, 200L)))
                .willReturn(List.of(cleaning1));

        PostAddCleaningsRequest request = new PostAddCleaningsRequest(List.of(100L, 200L));

        // when
        PostAddCleaningsResponse response = dutyService.addCleanings(request);

        // then
        assertThat(response.addedCleaningId()).containsExactly(100L);
        assertThat(cleaning1.getDuty()).isEqualTo(duty);
        verify(cleaningRepository).saveAll(anyList());
    }


    @Test
    @DisplayName("당번에서 청소 항목 제거 - 성공")
    void removeCleaningFromDuty_success() {
        // given
        Duty duty = Duty.builder()
                .name("탕비실 청소 당번")
                .icon(DISH_BLUE)
                .place(MemberContext.get().getPlace())
                .build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Cleaning cleaning = Cleaning.builder()
                .name("휴지통 비우기")
                .duty(duty)
                .build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 100L);

        given(cleaningRepository.findById(100L)).willReturn(Optional.of(cleaning));

        // when
        dutyService.removeCleaningFromDuty(100L);

        // then
        assertThat(cleaning.getDuty()).isNull();
    }


    @Test
    @DisplayName("당번에서 청소 항목 제거 - 존재하지 않는 청소 ID")
    void removeCleaningFromDuty_notFound() {
        // given
        Duty duty = Duty.builder()
                .name("탕비실 청소 당번")
                .icon(DISH_BLUE)
                .place(MemberContext.get().getPlace())
                .build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        given(cleaningRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dutyService.removeCleaningFromDuty(999L))
                .isInstanceOf(CleaningNotFoundException.class);
    }

    @Test
    @DisplayName("당번에서 청소 항목 제거 - 해당 Duty에 속하지 않은 청소")
    void removeCleaningFromDuty_notAssigned() {
        // given
        Duty duty = Duty.builder()
                .name("탕비실 청소 당번")
                .icon(DISH_BLUE)
                .place(MemberContext.get().getPlace())
                .build();
        ReflectionTestUtils.setField(duty, "dutyId", 1L);
        DutyContext.set(duty);

        Duty anotherDuty = Duty.builder()
                .name("회의실 청소 당번")
                .icon(TRASH_BLUE)
                .place(MemberContext.get().getPlace())
                .build();
        ReflectionTestUtils.setField(anotherDuty, "dutyId", 2L);

        Cleaning cleaning = Cleaning.builder()
                .name("회의실 책상 닦기")
                .duty(anotherDuty)
                .build();
        ReflectionTestUtils.setField(cleaning, "cleaningId", 200L);

        given(cleaningRepository.findById(200L)).willReturn(Optional.of(cleaning));

        // when & then
        assertThatThrownBy(() -> dutyService.removeCleaningFromDuty(200L))
                .isInstanceOf(CleaningNotAssignedException.class);
    }
}
