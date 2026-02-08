package com.dangbun.domain.duty.application.port.service;

import com.dangbun.domain.duty.adapter.out.persistence.FakeDutyRepository;
import com.dangbun.domain.duty.application.port.in.command.CreateDutyCommand;
import com.dangbun.domain.duty.application.port.in.command.UpdateDutyCommand;
import com.dangbun.domain.duty.application.port.in.query.UpdateDutyResult;
import com.dangbun.domain.duty.application.service.DutyCommandService;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.duty.domain.DutyIcon;
import com.dangbun.domain.duty.exception.custom.DutyAlreadyExistsException;
import com.dangbun.domain.duty.exception.custom.DutyNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DutyCommandServiceTest {

    private DutyCommandService dutyCommandService;
    private FakeDutyRepository fakeDutyRepository;

    @BeforeEach
    void setUp() {
        fakeDutyRepository = new FakeDutyRepository();

        dutyCommandService = new DutyCommandService(
                fakeDutyRepository,  // DutyCommandPort
                fakeDutyRepository,  // DutyQueryPort
                null,  // MemberDutyCommandPort - 필요시 Fake 구현
                null,  // MemberRepository - 필요시 Fake 구현
                null,  // SpringDataMemberDutyRepository
                null,  // CleaningRepository
                null,  // MemberCleaningRepository
                null   // SpringDataDutyRepository
        );
    }

    @Test
    void 당번_생성_성공() {
        // given
        CreateDutyCommand command = new CreateDutyCommand(1L, "청소 당번", DutyIcon.BROOM);

        // when
        Long dutyId = dutyCommandService.createDuty(command);

        // then
        assertThat(dutyId).isNotNull();
        assertThat(fakeDutyRepository.count()).isEqualTo(1);

        Duty saved = fakeDutyRepository.findById(dutyId).orElseThrow();
        assertThat(saved.getName()).isEqualTo("청소 당번");
        assertThat(saved.getIcon()).isEqualTo(DutyIcon.BROOM);
        assertThat(saved.getPlaceId()).isEqualTo(1L);
    }

    @Test
    void 당번_생성_실패_이미_존재하는_이름() {
        // given
        fakeDutyRepository.save(Duty.withoutId("청소 당번", DutyIcon.BROOM, 1L));
        CreateDutyCommand command = new CreateDutyCommand(1L, "청소 당번", DutyIcon.BROOM);

        // when, then
        assertThatThrownBy(() -> dutyCommandService.createDuty(command))
                .isInstanceOf(DutyAlreadyExistsException.class);

        assertThat(fakeDutyRepository.count()).isEqualTo(1);  // 추가되지 않음
    }

    @Test
    void 다른_플레이스에서_같은_이름_당번_생성_성공() {
        // given
        fakeDutyRepository.save(Duty.withoutId("청소 당번", DutyIcon.BROOM, 1L));
        CreateDutyCommand command = new CreateDutyCommand(2L, "청소 당번", DutyIcon.BROOM);

        // when
        Long dutyId = dutyCommandService.createDuty(command);

        // then
        assertThat(dutyId).isNotNull();
        assertThat(fakeDutyRepository.count()).isEqualTo(2);
    }

    @Test
    void 당번_수정_성공() {
        // given
        Duty existingDuty = fakeDutyRepository.save(Duty.withoutId("청소 당번", DutyIcon.BROOM, 1L));
        Long dutyId = existingDuty.getDutyId().value();

        UpdateDutyCommand command = new UpdateDutyCommand(dutyId, "수정된 당번", DutyIcon.TRASH);

        // when
        UpdateDutyResult result = dutyCommandService.updateDuty(command);

        // then
        assertThat(result.dutyId()).isEqualTo(dutyId);
        assertThat(result.name()).isEqualTo("수정된 당번");
        assertThat(result.icon()).isEqualTo(DutyIcon.TRASH);

        Duty updated = fakeDutyRepository.findById(dutyId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("수정된 당번");
        assertThat(updated.getIcon()).isEqualTo(DutyIcon.TRASH);
    }

    @Test
    void 당번_수정_실패_존재하지_않는_당번() {
        // given
        UpdateDutyCommand command = new UpdateDutyCommand(999L, "수정된 당번", DutyIcon.TRASH);

        // when, then
        assertThatThrownBy(() -> dutyCommandService.updateDuty(command))
                .isInstanceOf(DutyNotFoundException.class);
    }

    @Test
    void 당번_삭제_성공() {
        // given
        Duty existingDuty = fakeDutyRepository.save(Duty.withoutId("청소 당번", DutyIcon.BROOM, 1L));
        Long dutyId = existingDuty.getDutyId().value();

        assertThat(fakeDutyRepository.count()).isEqualTo(1);

        // when
        dutyCommandService.deleteDuty(dutyId);

        // then
        assertThat(fakeDutyRepository.count()).isEqualTo(0);
        assertThat(fakeDutyRepository.findById(dutyId)).isEmpty();
    }

    @Test
    void 당번_삭제_실패_존재하지_않는_당번() {
        // given
        Long nonExistentId = 999L;

        // when, then
        assertThatThrownBy(() -> dutyCommandService.deleteDuty(nonExistentId))
                .isInstanceOf(DutyNotFoundException.class);
    }

    @Test
    void 여러_당번_생성_후_조회() {
        // given
        Long placeId = 1L;
        CreateDutyCommand command1 = new CreateDutyCommand(placeId, "청소 당번", DutyIcon.BROOM);
        CreateDutyCommand command2 = new CreateDutyCommand(placeId, "설거지 당번", DutyIcon.DISH);
        CreateDutyCommand command3 = new CreateDutyCommand(placeId, "쓰레기 당번", DutyIcon.TRASH);

        // when
        dutyCommandService.createDuty(command1);
        dutyCommandService.createDuty(command2);
        dutyCommandService.createDuty(command3);

        // then
        assertThat(fakeDutyRepository.count()).isEqualTo(3);
        assertThat(fakeDutyRepository.findByPlaceId(placeId)).hasSize(3);
    }
}
