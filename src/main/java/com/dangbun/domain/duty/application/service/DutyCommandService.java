package com.dangbun.domain.duty.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningRepository;
import com.dangbun.domain.duty.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.duty.adapter.out.persistence.SpringDataDutyRepository;
import com.dangbun.domain.duty.application.port.in.command.*;
import com.dangbun.domain.duty.exception.custom.*;
import com.dangbun.domain.duty.application.port.in.query.AddCleaningsResult;
import com.dangbun.domain.duty.application.port.in.query.AddMembersResult;
import com.dangbun.domain.duty.application.port.in.query.UpdateDutyResult;
import com.dangbun.domain.duty.application.port.out.DutyCommandPort;
import com.dangbun.domain.duty.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRepository;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningJpaEntity;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningRepository;
import com.dangbun.domain.memberduty.application.port.out.MemberDutyCommandPort;
import com.dangbun.domain.memberduty.adapter.out.persistence.MemberDutyJpaEntity;
import com.dangbun.domain.memberduty.adapter.out.persistence.SpringDataMemberDutyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.dangbun.domain.duty.exception.status.DutyExceptionResponse.*;

@UseCase
@RequiredArgsConstructor
@Transactional
public class DutyCommandService implements DutyCommandUseCase {

    private final DutyCommandPort dutyCommandPort;
    private final DutyQueryPort dutyQueryPort;

    private final MemberDutyCommandPort memberDutyCommandPort;

    /*
     * TODO: Member 도메인 헥사고날 아키텍처 전환 시 수정
     * MemberRepository -> MemberQueryPort
     */
    private final MemberRepository memberRepository;

    /*
     * TODO: MemberDuty 도메인 헥사고날 아키텍처 전환 시 수정
     * SpringDataMemberDutyRepository -> MemberDutyQueryPort
     */
    private final SpringDataMemberDutyRepository memberDutyRepository;

    /*
     * TODO: Cleaning 도메인 헥사고날 아키텍처 전환 시 수정
     * CleaningRepository -> CleaningQueryPort/CommandPort
     */
    private final CleaningRepository cleaningRepository;

    /*
     * TODO: MemberCleaning 도메인 헥사고날 아키텍처 전환 시 수정
     * MemberCleaningRepository -> MemberCleaningQueryPort/CommandPort
     */
    private final MemberCleaningRepository memberCleaningRepository;

    @Override
    public Long createDuty(CreateDutyCommand command) {
        if (dutyQueryPort.existsByNameAndPlaceId(command.getName(), command.getPlaceId())) {
            throw new DutyAlreadyExistsException(DUTY_ALREADY_EXISTS);
        }

        Duty duty = Duty.withoutId(
                command.getName(),
                command.getIcon(),
                command.getPlaceId()
        );

        Duty saved = dutyCommandPort.save(duty);
        return saved.getDutyId().value();
    }

    @Override
    public UpdateDutyResult updateDuty(UpdateDutyCommand command) {
        Duty duty = dutyQueryPort.findById(command.getDutyId())
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        duty.update(command.getName(), command.getIcon());
        Duty updated = dutyCommandPort.save(duty);

        return UpdateDutyResult.from(updated);
    }

    @Override
    public void deleteDuty(Long dutyId) {
        Duty duty = dutyQueryPort.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        dutyCommandPort.delete(duty);
    }

    @Override
    public AddMembersResult addMembers(AddMembersCommand command) {
        /*
         * TODO: Duty 도메인 헥사고날 아키텍처 전환 시 DutyJpaEntity 참조 제거
         * 현재는 MemberDutyRepository가 DutyJpaEntity를 참조하므로 임시로 처리
         */
        DutyJpaEntity dutyEntity =
                findDutyJpaEntity(command.getDutyId());

        List<Long> requestedIds = command.getMemberIds();
        List<MemberJpaEntity> members = memberRepository.findAllById(requestedIds);

        if (members.size() != requestedIds.size()) {
            throw new MemberNotFoundException(MEMBER_NOT_FOUND);
        }

        memberDutyCommandPort.deleteAllByDutyId(dutyEntity.getDutyId());

        List<Long> addedMemberIds = new ArrayList<>();
        for (MemberJpaEntity member : members) {
            MemberDutyJpaEntity md = MemberDutyJpaEntity.builder()
                    .duty(dutyEntity)
                    .member(member)
                    .build();
            memberDutyRepository.save(md);
            addedMemberIds.add(member.getMemberId());
        }

        return AddMembersResult.of(addedMemberIds);
    }

    @Override
    public void assignMember(AssignMemberCommand command) {
        DutyJpaEntity dutyEntity =
                findDutyJpaEntity(command.getDutyId());

        List<CleaningJpaEntity> cleaningJpaEntities = cleaningRepository.findAllByDuty(dutyEntity);
        List<MemberJpaEntity> allMembers = memberDutyRepository.findMembersByDuty(dutyEntity);

        switch (command.getAssignType()) {
            case CUSTOM -> handleCustomAssign(command, dutyEntity);
            case COMMON -> handleCommonAssign(cleaningJpaEntities, allMembers);
            case RANDOM -> handleRandomAssign(command, cleaningJpaEntities, allMembers);
        }
    }

    private void handleCustomAssign(AssignMemberCommand command,
                                    DutyJpaEntity dutyEntity) {
        CleaningJpaEntity cleaningJpaEntity = cleaningRepository.findByCleaningIdAndDuty_DutyId(
                        command.getCleaningId(), dutyEntity.getDutyId())
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        memberCleaningRepository.deleteAllByCleaningJpaEntity_CleaningId(cleaningJpaEntity.getCleaningId());

        if (command.getMemberIds() == null) {
            return;
        }

        List<MemberJpaEntity> selectedMembers = memberRepository.findAllById(command.getMemberIds());
        List<MemberCleaningJpaEntity> mappings = selectedMembers.stream()
                .map(m -> MemberCleaningJpaEntity.builder().member(m).cleaningJpaEntity(cleaningJpaEntity).build())
                .toList();
        memberCleaningRepository.saveAll(mappings);
    }

    private void handleCommonAssign(List<CleaningJpaEntity> cleaningJpaEntities, List<MemberJpaEntity> allMembers) {
        if (allMembers.isEmpty()) {
            throw new MemberNotExistsException(MEMBER_NOT_EXISTS);
        }

        for (CleaningJpaEntity c : cleaningJpaEntities) {
            memberCleaningRepository.deleteAllByCleaningJpaEntity_CleaningId(c.getCleaningId());
            List<MemberCleaningJpaEntity> mappings = allMembers.stream()
                    .map(m -> MemberCleaningJpaEntity.builder().member(m).cleaningJpaEntity(c).build())
                    .toList();
            memberCleaningRepository.saveAll(mappings);
        }
    }

    private void handleRandomAssign(AssignMemberCommand command,
                                    List<CleaningJpaEntity> cleaningJpaEntities,
                                    List<MemberJpaEntity> allMembers) {
        Random random = new Random();

        for (CleaningJpaEntity cleaningJpaEntity : cleaningJpaEntities) {
            memberCleaningRepository.deleteAllByCleaningJpaEntity_CleaningId(cleaningJpaEntity.getCleaningId());

            List<MemberJpaEntity> shuffled = new ArrayList<>(allMembers);
            Collections.shuffle(shuffled, random);
            List<MemberJpaEntity> assigned = shuffled.stream()
                    .limit(command.getAssignCount())
                    .toList();

            List<MemberCleaningJpaEntity> mappings = assigned.stream()
                    .map(m -> MemberCleaningJpaEntity.builder().member(m).cleaningJpaEntity(cleaningJpaEntity).build())
                    .toList();
            memberCleaningRepository.saveAll(mappings);
        }
    }

    @Override
    public AddCleaningsResult addCleanings(AddCleaningsCommand command) {
        DutyJpaEntity dutyEntity =
                findDutyJpaEntity(command.getDutyId());

        List<CleaningJpaEntity> cleaningJpaEntities = cleaningRepository.findAllById(command.getCleaningIds());
        List<Long> assignedIds = new ArrayList<>();

        for (CleaningJpaEntity cleaningJpaEntity : cleaningJpaEntities) {
            if (cleaningJpaEntity.getDuty() == null) {
                cleaningJpaEntity.assignToDuty(dutyEntity);
                assignedIds.add(cleaningJpaEntity.getCleaningId());
            }
        }
        cleaningRepository.saveAll(cleaningJpaEntities);

        return AddCleaningsResult.of(assignedIds);
    }

    @Override
    public void removeCleaning(RemoveCleaningCommand command) {
        CleaningJpaEntity cleaningJpaEntity = cleaningRepository.findById(command.getCleaningId())
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        if (cleaningJpaEntity.getDuty() == null || !cleaningJpaEntity.getDuty().getDutyId().equals(command.getDutyId())) {
            throw new CleaningNotAssignedException(CLEANING_NOT_ASSIGNED);
        }

        cleaningJpaEntity.removeDuty();
    }

    /*
     * TODO: 임시 메서드 - MemberDuty/Cleaning 리팩토링 완료 후 제거
     * 현재 MemberDutyRepository, CleaningRepository가 DutyJpaEntity를 참조하므로
     * DutyJpaEntity를 직접 조회하는 임시 메서드
     */
    private DutyJpaEntity findDutyJpaEntity(Long dutyId) {
        return dutyJpaEntityRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));
    }

    /*
     * TODO: 임시 의존성 - MemberDuty/Cleaning 리팩토링 완료 후 제거
     */
    private final SpringDataDutyRepository dutyJpaEntityRepository;
}
