package com.dangbun.domain.memberduty.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.memberduty.application.port.out.MemberDutyCommandPort;
import com.dangbun.domain.memberduty.application.port.out.MemberDutyQueryPort;
import com.dangbun.domain.memberduty.domain.MemberDuty;
import lombok.RequiredArgsConstructor;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
class MemberDutyPersistenceAdapter implements MemberDutyCommandPort, MemberDutyQueryPort {

    private final SpringDataMemberDutyRepository memberDutyRepository;
    private final MemberDutyMapper memberDutyMapper;

    @Override
    public void save(MemberDuty memberDuty) {
        MemberDutyJpaEntity jpaEntity = memberDutyMapper.mapToJpaEntity(memberDuty);
        memberDutyRepository.save(jpaEntity);
    }

    @Override
    public void saveAll(List<MemberDuty> memberDuties) {
        List<MemberDutyJpaEntity> jpaEntities = memberDuties.stream()
                .map(memberDutyMapper::mapToJpaEntity)
                .toList();
        memberDutyRepository.saveAll(jpaEntities);
    }

    @Override
    public void deleteAllByDutyId(Long dutyId) {
        memberDutyRepository.deleteAllByDutyId(dutyId);
    }

    @Override
    public void deleteByMemberIdAndDutyId(Long memberId, Long dutyId) {
        memberDutyRepository.deleteByMemberIdAndDutyId(memberId, dutyId);
    }

    @Override
    public List<MemberDuty> findAllByPlaceId(Long placeId) {
        return memberDutyRepository.findAllWithMemberByPlaceId(placeId).stream()
                .map(memberDutyMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public List<MemberDuty> findAllByDutyId(Long dutyId) {
        return memberDutyRepository.findAllByDuty_DutyId(dutyId).stream()
                .map(memberDutyMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public List<MemberDuty> findAllByMemberId(Long memberId) {
        return memberDutyRepository.findAllByMemberId(memberId).stream()
                .map(memberDutyMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public List<Long> findMemberIdsByDutyId(Long dutyId) {
        return memberDutyRepository.findMemberIdsByDutyId(dutyId);
    }

    @Override
    public boolean existsByDutyIdAndMemberId(Long dutyId, Long memberId) {
        return memberDutyRepository.existsByDuty_DutyIdAndMember_MemberId(dutyId, memberId);
    }

    @Override
    public List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds) {
        return memberDutyRepository.findDistinctDutiesByMemberIds(memberIds).stream()
                .map(memberDutyMapper::mapDutyToDomainEntity)
                .toList();
    }

    @Override
    public List<MemberDutyMemberInfo> findMemberInfosByDutyId(Long dutyId) {
        return memberDutyRepository.findAllByDuty_DutyId(dutyId).stream()
                .map(md -> new MemberDutyMemberInfo(
                        md.getMember().getMemberId(),
                        md.getMember().getRole().name(),
                        md.getMember().getName()
                ))
                .toList();
    }

    @Override
    public List<MemberDutyDutyInfo> findDutyInfosByMemberId(Long memberId) {
        return memberDutyRepository.findAllByMember_MemberId(memberId).stream()
                .map(md -> new MemberDutyDutyInfo(
                        md.getDuty().getDutyId(),
                        md.getDuty().getName()
                ))
                .toList();
    }
}
