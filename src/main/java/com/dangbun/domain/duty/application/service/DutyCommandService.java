package com.dangbun.domain.duty.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.cleaning.application.port.in.command.CleaningForDutyUseCase;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningForDutyQuery;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningForDutyQuery.CleaningInfo;
import com.dangbun.domain.duty.application.port.in.command.*;
import com.dangbun.domain.duty.exception.custom.*;
import com.dangbun.domain.duty.application.port.in.query.AddCleaningsResult;
import com.dangbun.domain.duty.application.port.in.query.AddMembersResult;
import com.dangbun.domain.duty.application.port.in.query.UpdateDutyResult;
import com.dangbun.domain.duty.application.port.out.DutyCommandPort;
import com.dangbun.domain.duty.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.member.application.port.in.query.GetMembersForDutyQuery;
import com.dangbun.domain.member.application.port.in.query.GetMembersForDutyQuery.MemberInfo;
import com.dangbun.domain.membercleaning.application.port.in.command.MemberCleaningForDutyUseCase;
import com.dangbun.domain.memberduty.application.port.in.command.MemberDutyForDutyUseCase;
import com.dangbun.domain.memberduty.application.port.in.query.GetMemberDutyForDutyQuery;
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

    private final GetMembersForDutyQuery getMembersForDutyQuery;
    private final MemberDutyForDutyUseCase memberDutyForDutyUseCase;
    private final GetMemberDutyForDutyQuery getMemberDutyForDutyQuery;

    private final GetCleaningForDutyQuery getCleaningForDutyQuery;
    private final CleaningForDutyUseCase cleaningForDutyUseCase;

    private final MemberCleaningForDutyUseCase memberCleaningForDutyUseCase;

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
        validateDutyExists(command.getDutyId());

        List<Long> requestedIds = command.getMemberIds();
        List<MemberInfo> members = getMembersForDutyQuery.findAllByIds(requestedIds);

        if (members.size() != requestedIds.size()) {
            throw new MemberNotFoundException(MEMBER_NOT_FOUND);
        }

        memberDutyForDutyUseCase.deleteAllByDutyId(command.getDutyId());
        memberDutyForDutyUseCase.saveAllByDutyId(command.getDutyId(), requestedIds);

        List<Long> addedMemberIds = members.stream()
                .map(MemberInfo::memberId)
                .toList();

        return AddMembersResult.of(addedMemberIds);
    }

    @Override
    public void assignMember(AssignMemberCommand command) {
        validateDutyExists(command.getDutyId());

        List<CleaningInfo> cleanings = getCleaningForDutyQuery.findAllByDutyId(command.getDutyId());
        List<Long> memberIds = getMemberDutyForDutyQuery.findMemberIdsByDutyId(command.getDutyId());
        List<MemberInfo> allMembers = getMembersForDutyQuery.findAllByIds(memberIds);

        switch (command.getAssignType()) {
            case CUSTOM -> handleCustomAssign(command);
            case COMMON -> handleCommonAssign(cleanings, allMembers);
            case RANDOM -> handleRandomAssign(command, cleanings, allMembers);
        }
    }

    private void handleCustomAssign(AssignMemberCommand command) {
        CleaningInfo cleaning = getCleaningForDutyQuery.findByCleaningIdAndDutyId(
                        command.getCleaningId(), command.getDutyId())
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        memberCleaningForDutyUseCase.deleteAllByCleaningId(cleaning.cleaningId());

        if (command.getMemberIds() == null) {
            return;
        }

        memberCleaningForDutyUseCase.saveAllByCleaningIdAndMemberIds(cleaning.cleaningId(), command.getMemberIds());
    }

    private void handleCommonAssign(List<CleaningInfo> cleanings, List<MemberInfo> allMembers) {
        if (allMembers.isEmpty()) {
            throw new MemberNotExistsException(MEMBER_NOT_EXISTS);
        }

        List<Long> memberIds = allMembers.stream()
                .map(MemberInfo::memberId)
                .toList();

        for (CleaningInfo cleaning : cleanings) {
            memberCleaningForDutyUseCase.deleteAllByCleaningId(cleaning.cleaningId());
            memberCleaningForDutyUseCase.saveAllByCleaningIdAndMemberIds(cleaning.cleaningId(), memberIds);
        }
    }

    private void handleRandomAssign(AssignMemberCommand command,
                                    List<CleaningInfo> cleanings,
                                    List<MemberInfo> allMembers) {
        Random random = new Random();

        for (CleaningInfo cleaning : cleanings) {
            memberCleaningForDutyUseCase.deleteAllByCleaningId(cleaning.cleaningId());

            List<MemberInfo> shuffled = new ArrayList<>(allMembers);
            Collections.shuffle(shuffled, random);
            List<Long> assignedMemberIds = shuffled.stream()
                    .limit(command.getAssignCount())
                    .map(MemberInfo::memberId)
                    .toList();

            memberCleaningForDutyUseCase.saveAllByCleaningIdAndMemberIds(cleaning.cleaningId(), assignedMemberIds);
        }
    }

    @Override
    public AddCleaningsResult addCleanings(AddCleaningsCommand command) {
        validateDutyExists(command.getDutyId());

        List<CleaningInfo> cleanings = getCleaningForDutyQuery.findAllByIds(command.getCleaningIds());
        List<Long> unassignedIds = cleanings.stream()
                .filter(c -> c.dutyId() == null)
                .map(CleaningInfo::cleaningId)
                .toList();

        if (!unassignedIds.isEmpty()) {
            cleaningForDutyUseCase.assignCleaningsToDuty(command.getDutyId(), unassignedIds);
        }

        return AddCleaningsResult.of(unassignedIds);
    }

    @Override
    public void removeCleaning(RemoveCleaningCommand command) {
        CleaningInfo cleaning = getCleaningForDutyQuery.findById(command.getCleaningId())
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        if (cleaning.dutyId() == null || !cleaning.dutyId().equals(command.getDutyId())) {
            throw new CleaningNotAssignedException(CLEANING_NOT_ASSIGNED);
        }

        cleaningForDutyUseCase.removeCleaningFromDuty(command.getCleaningId());
    }

    private void validateDutyExists(Long dutyId) {
        dutyQueryPort.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));
    }
}
