package com.dangbun.domain.memberduty.refactor.adapter.out;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.duty.original.repository.DutyRepository;
import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.member.original.repository.MemberRepository;
import com.dangbun.domain.memberduty.refactor.MemberDuty;
import com.dangbun.domain.memberduty.refactor.MemberDutyCommandPort;
import com.dangbun.domain.memberduty.refactor.MemberDutyQueryPort;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@PersistenceAdapter
@RequiredArgsConstructor
public class MemberDutyPersistenceAdapter implements MemberDutyCommandPort, MemberDutyQueryPort {

    private final MemberRepository memberRepository;
    private final DutyRepository dutyRepository;
    private final MemberDutyRepository memberDutyRepository;

    private final MemberDutyMapper mdMapper;

    @Override
    public void save(MemberDuty memberDuty) {
        MemberJpaEntity memberJpaEntity = memberRepository.getReferenceById(memberDuty.getId().getMemberId());
        DutyJpaEntity duty = dutyRepository.getReferenceById(memberDuty.getId().getDutyId());

        MemberDutyJpaEntity jpaEntity = MemberDutyJpaEntity.builder()
                .member(memberJpaEntity)
                .duty(duty)
                .build();

        memberDutyRepository.save(jpaEntity);
    }

    @Override
    public void deleteAllByDutyId(Long dutyId) {
        DutyJpaEntity duty = dutyRepository.getReferenceById(dutyId);
        memberDutyRepository.deleteAllByDuty(duty);
    }

    @Override
    public List<MemberDuty> findAllWithMemberAndPlaceByPlaceId(Long placeId) {
        List<MemberDutyJpaEntity> mdJpaEntities = memberDutyRepository.findAllWithMemberAndPlaceByPlaceId(placeId);
        List<MemberDuty> mds = mdJpaEntities.stream().map(mdMapper::mapToDomainEntity).toList();
        return mds;
    }
}
