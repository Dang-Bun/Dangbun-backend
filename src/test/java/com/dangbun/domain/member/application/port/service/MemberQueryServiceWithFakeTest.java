package com.dangbun.domain.member.application.port.service;

import com.dangbun.domain.member.adapter.out.persistence.FakeMemberRepository;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.application.port.in.query.*;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.member.exception.custom.MemberNotFoundException;
import com.dangbun.domain.memberduty.application.port.in.query.FakeGetMemberDutyForMemberQuery;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.domain.place.domain.PlaceCategory;
import com.dangbun.global.context.MemberContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MemberPageQueryService 테스트 - Fake 객체 사용
 * Mock 대신 인메모리 Fake 저장소를 사용하여 실제 동작을 검증
 */
class MemberQueryServiceWithFakeTest {

    private MemberPageQueryService memberPageQueryService;

    private FakeMemberRepository fakeMemberRepository;
    private FakeGetMemberDutyForMemberQuery fakeGetMemberDutyForMemberQuery;

    private PlaceJpaEntity mockPlace;
    private MemberJpaEntity mockContextMember;

    @BeforeEach
    void setUp() {
        // Fake 저장소 초기화
        fakeMemberRepository = new FakeMemberRepository();
        fakeGetMemberDutyForMemberQuery = new FakeGetMemberDutyForMemberQuery();

        // 서비스 생성
        memberPageQueryService = new MemberPageQueryService(
                fakeMemberRepository,
                fakeGetMemberDutyForMemberQuery
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
        fakeGetMemberDutyForMemberQuery.clear();
    }

    @Test
    void 멤버_목록_조회_매니저_대기멤버수_포함() {
        // given
        Member manager = createMember(1L, "매니저", com.dangbun.domain.member.domain.MemberRole.MANAGER, true, 1L);
        Member member1 = createMember(2L, "멤버1", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
        Member waitingMember = createMember(3L, "대기 멤버", com.dangbun.domain.member.domain.MemberRole.WAITING, false, 1L);

        fakeMemberRepository.save(manager);
        fakeMemberRepository.save(member1);
        fakeMemberRepository.save(waitingMember);

        fakeGetMemberDutyForMemberQuery.addDutyInfoForMember(1L, 10L, "청소 당번");
        fakeGetMemberDutyForMemberQuery.addDutyInfoForMember(2L, 10L, "청소 당번");

        // when
        MembersResult result = memberPageQueryService.getMembers();

        // then
        assertThat(result.waitingMemberNumber()).isEqualTo(1);
        assertThat(result.members()).hasSize(2); // 활성화된 멤버만 (매니저, 멤버1)
        assertThat(result.members().get(0).name()).isEqualTo("매니저"); // 매니저가 먼저
    }

    @Test
    void 멤버_목록_조회_일반멤버_대기멤버수_null() {
        // given
        mockContextMember = MemberJpaEntity.builder()
                .name("일반 멤버")
                .place(mockPlace)
                .role(MemberRole.MEMBER)
                .status(true)
                .build();
        ReflectionTestUtils.setField(mockContextMember, "memberId", 100L);
        MemberContext.set(mockContextMember);

        Member member = createMember(1L, "멤버1", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
        Member waitingMember = createMember(2L, "대기 멤버", com.dangbun.domain.member.domain.MemberRole.WAITING, false, 1L);

        fakeMemberRepository.save(member);
        fakeMemberRepository.save(waitingMember);

        // when
        MembersResult result = memberPageQueryService.getMembers();

        // then
        assertThat(result.waitingMemberNumber()).isNull();
    }

    @Test
    void 멤버_목록_조회_duty정보_포함() {
        // given
        Member member = createMember(1L, "멤버", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
        fakeMemberRepository.save(member);

        fakeGetMemberDutyForMemberQuery.addDutyInfoForMember(1L, 10L, "청소 당번");
        fakeGetMemberDutyForMemberQuery.addDutyInfoForMember(1L, 20L, "주방 당번");

        // when
        MembersResult result = memberPageQueryService.getMembers();

        // then
        assertThat(result.members()).hasSize(1);
        assertThat(result.members().get(0).dutyNames()).containsExactlyInAnyOrder("청소 당번", "주방 당번");
    }

    @Test
    void 멤버_상세_조회_성공() {
        // given
        Member member = createMember(1L, "멤버", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
        fakeMemberRepository.save(member);

        fakeGetMemberDutyForMemberQuery.addDutyInfoForMember(1L, 10L, "청소 당번");

        // when
        MemberDetailResult result = memberPageQueryService.getMember(1L);

        // then
        assertThat(result.member().name()).isEqualTo("멤버");
        assertThat(result.member().role()).isEqualTo("멤버");
        assertThat(result.duties()).hasSize(1);
        assertThat(result.duties().get(0).dutyId()).isEqualTo(10L);
        assertThat(result.duties().get(0).dutyName()).isEqualTo("청소 당번");
    }

    @Test
    void 멤버_상세_조회_실패_존재하지_않는_멤버() {
        // given
        // 멤버가 없는 상태

        // when, then
        assertThatThrownBy(() -> memberPageQueryService.getMember(999L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void 멤버_상세_조회_다른_플레이스_멤버_조회_실패() {
        // given
        Member otherPlaceMember = createMember(1L, "다른 플레이스 멤버", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 999L);
        fakeMemberRepository.save(otherPlaceMember);

        // when, then
        assertThatThrownBy(() -> memberPageQueryService.getMember(1L))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void 대기_멤버_목록_조회() {
        // given
        Member waitingMember1 = createMember(1L, "대기 멤버1", com.dangbun.domain.member.domain.MemberRole.WAITING, false, 1L);
        Member waitingMember2 = createMember(2L, "대기 멤버2", com.dangbun.domain.member.domain.MemberRole.WAITING, false, 1L);
        Member activeMember = createMember(3L, "활성 멤버", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);

        fakeMemberRepository.save(waitingMember1);
        fakeMemberRepository.save(waitingMember2);
        fakeMemberRepository.save(activeMember);

        // when
        WaitingMembersResult result = memberPageQueryService.getWaitingMembers();

        // then
        assertThat(result.members()).hasSize(2);
    }

    @Test
    void 대기_멤버_목록_조회_다른_플레이스는_제외() {
        // given
        Member waitingMember = createMember(1L, "대기 멤버", com.dangbun.domain.member.domain.MemberRole.WAITING, false, 1L);
        Member otherPlaceWaitingMember = createMember(2L, "다른 플레이스 대기 멤버", com.dangbun.domain.member.domain.MemberRole.WAITING, false, 999L);

        fakeMemberRepository.save(waitingMember);
        fakeMemberRepository.save(otherPlaceWaitingMember);

        // when
        WaitingMembersResult result = memberPageQueryService.getWaitingMembers();

        // then
        assertThat(result.members()).hasSize(1);
        assertThat(result.members().get(0).name()).isEqualTo("대기 멤버");
    }

    @Test
    void 내_정보_조회() {
        // given
        // mockContextMember가 이미 설정됨

        // when
        MyInformationResult result = memberPageQueryService.getMyInformation();

        // then
        assertThat(result.memberId()).isEqualTo(100L);
        assertThat(result.memberName()).isEqualTo("매니저");
        assertThat(result.memberRole()).isEqualTo("매니저");
    }

    @Test
    void 이름으로_멤버_검색_성공() {
        // given
        Member member = createMember(1L, "홍길동", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
        fakeMemberRepository.save(member);

        // when
        MemberSearchResult result = memberPageQueryService.searchByNameInPlace(1L, "홍길동");

        // then
        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("홍길동");
    }

    @Test
    void 이름으로_멤버_검색_실패_존재하지_않는_이름() {
        // given
        // 멤버가 없는 상태

        // when
        MemberSearchResult result = memberPageQueryService.searchByNameInPlace(1L, "없는 이름");

        // then
        assertThat(result.memberId()).isNull();
        assertThat(result.name()).isNull();
    }

    @Test
    void ID목록으로_멤버_조회() {
        // given
        Member member1 = createMember(1L, "멤버1", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
        Member member2 = createMember(2L, "멤버2", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
        Member member3 = createMember(3L, "멤버3", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);

        fakeMemberRepository.save(member1);
        fakeMemberRepository.save(member2);
        fakeMemberRepository.save(member3);

        // when
        List<Member> result = memberPageQueryService.findAllByIds(List.of(1L, 2L));

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 플레이스별_멤버_페이징_조회() {
        // given
        for (int i = 1; i <= 15; i++) {
            Member member = createMember((long) i, "멤버" + i, com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
            fakeMemberRepository.save(member);
        }

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Member> result = memberPageQueryService.getPagedMemberByPlaceId(1L, pageable);

        // then
        assertThat(result.getContent()).hasSize(15); // FakeMemberRepository는 페이징을 실제로 적용하지 않음
        assertThat(result.getTotalElements()).isEqualTo(15);
    }

    @Test
    void 플레이스별_이름으로_멤버_검색_페이징() {
        // given
        Member member1 = createMember(1L, "홍길동", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
        Member member2 = createMember(2L, "홍길순", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
        Member member3 = createMember(3L, "김철수", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);

        fakeMemberRepository.save(member1);
        fakeMemberRepository.save(member2);
        fakeMemberRepository.save(member3);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Member> result = memberPageQueryService.getPageMemberByPlaceIdAndNameContaining(1L, "홍", pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void ID로_멤버_조회() {
        // given
        Member member = createMember(1L, "멤버", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
        fakeMemberRepository.save(member);

        // when
        Member result = memberPageQueryService.getMemberById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("멤버");
    }

    @Test
    void ID로_멤버_조회_존재하지_않으면_null() {
        // given
        // 멤버가 없는 상태

        // when
        Member result = memberPageQueryService.getMemberById(999L);

        // then
        assertThat(result).isNull();
    }

    @Test
    void 멤버_목록_정렬_매니저가_먼저_그다음_이름순() {
        // given
        Member member1 = createMember(1L, "김철수", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
        Member member2 = createMember(2L, "박영희", com.dangbun.domain.member.domain.MemberRole.MEMBER, true, 1L);
        Member manager = createMember(3L, "이매니저", com.dangbun.domain.member.domain.MemberRole.MANAGER, true, 1L);

        fakeMemberRepository.save(member1);
        fakeMemberRepository.save(member2);
        fakeMemberRepository.save(manager);

        // when
        MembersResult result = memberPageQueryService.getMembers();

        // then
        assertThat(result.members()).hasSize(3);
        assertThat(result.members().get(0).name()).isEqualTo("이매니저"); // 매니저가 먼저
        assertThat(result.members().get(1).name()).isEqualTo("김철수"); // 이름순 정렬
        assertThat(result.members().get(2).name()).isEqualTo("박영희");
    }

    private Member createMember(Long memberId, String name, com.dangbun.domain.member.domain.MemberRole role, Boolean status, Long placeId) {
        return Member.withId(
                memberId,
                role,
                name,
                status,
                Map.of("key", "value"),
                placeId,
                1L,
                LocalDateTime.now()
        );
    }
}
