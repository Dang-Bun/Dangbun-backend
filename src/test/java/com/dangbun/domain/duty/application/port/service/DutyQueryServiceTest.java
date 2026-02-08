package com.dangbun.domain.duty.application.port.service;

import com.dangbun.domain.duty.adapter.out.persistence.FakeDutyRepository;
import com.dangbun.domain.duty.application.port.in.query.DutyListResult;
import com.dangbun.domain.duty.application.service.DutyQueryService;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.duty.domain.DutyIcon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DutyQueryServiceTest {

    private DutyQueryService dutyQueryService;
    private FakeDutyRepository fakeDutyRepository;

    @BeforeEach
    void setUp() {
        fakeDutyRepository = new FakeDutyRepository();

        dutyQueryService = new DutyQueryService(
                fakeDutyRepository,  // DutyQueryPort
                null,  // SpringDataMemberDutyRepository
                null,  // CleaningRepository
                null,  // MemberCleaningRepository
                null   // SpringDataDutyRepository
        );
    }

    @Test
    void 당번_목록_조회_성공() {
        // given
        Long placeId = 1L;
        fakeDutyRepository.save(Duty.withoutId("청소 당번", DutyIcon.BROOM, placeId));
        fakeDutyRepository.save(Duty.withoutId("설거지 당번", DutyIcon.DISH, placeId));

        // when
        DutyListResult result = dutyQueryService.getDutyList(placeId);

        // then
        assertThat(result.duties()).hasSize(2);
        assertThat(result.duties())
                .extracting(DutyListResult.DutyItem::name)
                .containsExactlyInAnyOrder("청소 당번", "설거지 당번");
    }

    @Test
    void 당번_목록_조회_빈_목록() {
        // given
        Long placeId = 1L;

        // when
        DutyListResult result = dutyQueryService.getDutyList(placeId);

        // then
        assertThat(result.duties()).isEmpty();
    }

    @Test
    void 다른_플레이스_당번은_조회되지_않음() {
        // given
        fakeDutyRepository.save(Duty.withoutId("청소 당번", DutyIcon.BROOM, 1L));
        fakeDutyRepository.save(Duty.withoutId("설거지 당번", DutyIcon.DISH, 2L));

        // when
        DutyListResult result = dutyQueryService.getDutyList(1L);

        // then
        assertThat(result.duties()).hasSize(1);
        assertThat(result.duties().get(0).name()).isEqualTo("청소 당번");
    }

    @Test
    void 당번_목록_아이콘_정보_포함() {
        // given
        Long placeId = 1L;
        fakeDutyRepository.save(Duty.withoutId("청소 당번", DutyIcon.BROOM, placeId));
        fakeDutyRepository.save(Duty.withoutId("쓰레기 당번", DutyIcon.TRASH, placeId));

        // when
        DutyListResult result = dutyQueryService.getDutyList(placeId);

        // then
        assertThat(result.duties())
                .extracting(DutyListResult.DutyItem::icon)
                .containsExactlyInAnyOrder(DutyIcon.BROOM, DutyIcon.TRASH);
    }

    @Test
    void 여러_플레이스_각각_조회() {
        // given
        fakeDutyRepository.save(Duty.withoutId("청소 당번", DutyIcon.BROOM, 1L));
        fakeDutyRepository.save(Duty.withoutId("설거지 당번", DutyIcon.DISH, 1L));
        fakeDutyRepository.save(Duty.withoutId("쓰레기 당번", DutyIcon.TRASH, 2L));

        // when
        DutyListResult result1 = dutyQueryService.getDutyList(1L);
        DutyListResult result2 = dutyQueryService.getDutyList(2L);
        DutyListResult result3 = dutyQueryService.getDutyList(3L);

        // then
        assertThat(result1.duties()).hasSize(2);
        assertThat(result2.duties()).hasSize(1);
        assertThat(result3.duties()).isEmpty();
    }
}
