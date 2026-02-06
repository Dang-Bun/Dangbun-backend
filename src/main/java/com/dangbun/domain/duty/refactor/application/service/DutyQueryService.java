package com.dangbun.domain.duty.refactor.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningRepository;
import com.dangbun.domain.duty.refactor.application.port.in.query.*;
import com.dangbun.domain.duty.refactor.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.duty.refactor.exception.custom.DutyNotFoundException;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.member.original.entity.MemberRole;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningJpaEntity;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningRepository;
import com.dangbun.domain.memberduty.refactor.adapter.out.MemberDutyJpaEntity;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static com.dangbun.domain.duty.refactor.exception.status.DutyExceptionResponse.DUTY_NOT_FOUND;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DutyQueryService implements DutyQuery {

    private final DutyQueryPort dutyQueryPort;

    /*
     * TODO: MemberDuty 도메인 헥사고날 아키텍처 전환 시 수정
     * MemberDutyRepository -> MemberDutyQueryPort
     */
    private final MemberDutyRepository memberDutyRepository;

    /*
     * TODO: Cleaning 도메인 헥사고날 아키텍처 전환 시 수정
     * CleaningRepository -> CleaningQueryPort
     */
    private final CleaningRepository cleaningRepository;

    /*
     * TODO: MemberCleaning 도메인 헥사고날 아키텍처 전환 시 수정
     * MemberCleaningRepository -> MemberCleaningQueryPort
     */
    private final MemberCleaningRepository memberCleaningRepository;

    /*
     * TODO: 임시 의존성 - MemberDuty/Cleaning 리팩토링 완료 후 제거
     */
    private final com.dangbun.domain.duty.refactor.adapter.out.persistence.SpringDataDutyRepository dutyJpaEntityRepository;

    @Override
    public DutyListResult getDutyList(Long placeId) {
        List<Duty> duties = dutyQueryPort.findByPlaceId(placeId);
        return DutyListResult.from(duties);
    }

    @Override
    public DutyMembersResult getDutyMembers(Long dutyId) {
        com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity dutyEntity =
                findDutyJpaEntity(dutyId);

        List<MemberDutyJpaEntity> memberDuties = memberDutyRepository.findAllByDuty(dutyEntity);

        List<DutyMembersResult.MemberItem> members = memberDuties.stream()
                .map(MemberDutyJpaEntity::getMember)
                .sorted(
                        Comparator
                                .comparing((MemberJpaEntity m) -> m.getRole() != MemberRole.MANAGER)
                                .thenComparing(MemberJpaEntity::getName, Comparator.nullsLast(String::compareTo))
                )
                .map(m -> new DutyMembersResult.MemberItem(
                        m.getMemberId(),
                        m.getRole().name(),
                        m.getName()
                ))
                .toList();

        return DutyMembersResult.of(members);
    }

    @Override
    public DutyCleaningsResult getDutyCleanings(Long dutyId) {
        com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity dutyEntity =
                findDutyJpaEntity(dutyId);

        List<CleaningJpaEntity> cleaningJpaEntities = cleaningRepository.findAllByDuty(dutyEntity);

        List<DutyCleaningsResult.CleaningItem> items = cleaningJpaEntities.stream()
                .map(c -> new DutyCleaningsResult.CleaningItem(
                        c.getCleaningId(),
                        c.getName()
                ))
                .toList();

        return DutyCleaningsResult.of(items);
    }

    @Override
    public CleaningInfoListResult getCleaningInfoList(Long dutyId) {
        com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity dutyEntity =
                findDutyJpaEntity(dutyId);

        List<CleaningJpaEntity> cleaningJpaEntities = cleaningRepository.findAllByDuty(dutyEntity);

        List<CleaningInfoListResult.CleaningInfo> infos = cleaningJpaEntities.stream()
                .map(cleaning -> {
                    List<MemberCleaningJpaEntity> mappings = memberCleaningRepository.findAllByCleaningJpaEntity(cleaning);
                    List<MemberJpaEntity> members = mappings.stream()
                            .map(MemberCleaningJpaEntity::getMember)
                            .distinct()
                            .toList();

                    List<String> displayedNames = members.stream()
                            .map(MemberJpaEntity::getName)
                            .limit(2)
                            .toList();

                    return new CleaningInfoListResult.CleaningInfo(
                            cleaning.getCleaningId(),
                            cleaning.getName(),
                            displayedNames,
                            members.size()
                    );
                })
                .toList();

        return CleaningInfoListResult.of(infos);
    }

    /*
     * TODO: 임시 메서드 - MemberDuty/Cleaning 리팩토링 완료 후 제거
     */
    private com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity findDutyJpaEntity(Long dutyId) {
        return dutyJpaEntityRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));
    }
}
