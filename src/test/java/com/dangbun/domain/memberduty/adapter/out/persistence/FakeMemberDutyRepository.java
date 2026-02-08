package com.dangbun.domain.memberduty.adapter.out.persistence;

import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.memberduty.application.port.out.MemberDutyCommandPort;
import com.dangbun.domain.memberduty.application.port.out.MemberDutyQueryPort;
import com.dangbun.domain.memberduty.domain.MemberDuty;

import java.util.*;
import java.util.function.Function;

/**
 * 테스트용 인메모리 MemberDuty 저장소
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeMemberDutyRepository implements MemberDutyQueryPort, MemberDutyCommandPort {

    private final List<MemberDuty> storage = new ArrayList<>();
    private final Map<Long, Duty> dutyStorage = new HashMap<>();

    @Override
    public List<MemberDuty> findAllByPlaceId(Long placeId) {
        return storage.stream()
                .filter(md -> {
                    Duty duty = dutyStorage.get(md.getDutyId());
                    return duty != null && duty.getPlaceId().equals(placeId);
                })
                .toList();
    }

    @Override
    public List<MemberDuty> findAllByDutyId(Long dutyId) {
        return storage.stream()
                .filter(md -> md.getDutyId().equals(dutyId))
                .toList();
    }

    @Override
    public List<MemberDuty> findAllByMemberId(Long memberId) {
        return storage.stream()
                .filter(md -> md.getMemberId().equals(memberId))
                .toList();
    }

    @Override
    public List<Long> findMemberIdsByDutyId(Long dutyId) {
        return storage.stream()
                .filter(md -> md.getDutyId().equals(dutyId))
                .map(MemberDuty::getMemberId)
                .toList();
    }

    @Override
    public boolean existsByDutyIdAndMemberId(Long dutyId, Long memberId) {
        return storage.stream()
                .anyMatch(md -> md.getDutyId().equals(dutyId) && md.getMemberId().equals(memberId));
    }

    @Override
    public List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds) {
        Set<Long> dutyIds = storage.stream()
                .filter(md -> memberIds.contains(md.getMemberId()))
                .map(MemberDuty::getDutyId)
                .collect(java.util.stream.Collectors.toSet());

        return dutyIds.stream()
                .map(dutyStorage::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<MemberDutyMemberInfo> findMemberInfosByDutyId(Long dutyId) {
        return List.of();
    }

    @Override
    public List<MemberDutyDutyInfo> findDutyInfosByMemberId(Long memberId) {
        return storage.stream()
                .filter(md -> md.getMemberId().equals(memberId))
                .map(md -> {
                    Duty duty = dutyStorage.get(md.getDutyId());
                    return new MemberDutyDutyInfo(md.getDutyId(), duty != null ? duty.getName() : null);
                })
                .toList();
    }

    @Override
    public void save(MemberDuty memberDuty) {
        storage.removeIf(md ->
                md.getMemberId().equals(memberDuty.getMemberId()) &&
                md.getDutyId().equals(memberDuty.getDutyId())
        );
        storage.add(memberDuty);
    }

    @Override
    public void saveAll(List<MemberDuty> memberDuties) {
        for (MemberDuty md : memberDuties) {
            save(md);
        }
    }

    @Override
    public void deleteByMemberIdAndDutyId(Long memberId, Long dutyId) {
        storage.removeIf(md -> md.getDutyId().equals(dutyId) && md.getMemberId().equals(memberId));
    }

    @Override
    public void deleteAllByDutyId(Long dutyId) {
        storage.removeIf(md -> md.getDutyId().equals(dutyId));
    }

    public void addDuty(Duty duty) {
        dutyStorage.put(duty.getDutyId().value(), duty);
    }

    public void clear() {
        storage.clear();
        dutyStorage.clear();
    }

    public int count() {
        return storage.size();
    }
}
