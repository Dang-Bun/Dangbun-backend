package com.dangbun.domain.duty.refactor.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.duty.refactor.application.port.out.DutyCommandPort;
import com.dangbun.domain.duty.refactor.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@PersistenceAdapter
@RequiredArgsConstructor
class DutyPersistenceAdapter implements DutyCommandPort, DutyQueryPort {

    private final SpringDataDutyRepository dutyRepository;
    private final MemberDutyRepository memberDutyRepository;
    private final DutyMapper dutyMapper;

    @Override
    public Duty save(Duty duty) {
        DutyJpaEntity entity = dutyMapper.mapToJpaEntity(duty);
        DutyJpaEntity saved = dutyRepository.save(entity);
        return dutyMapper.mapToDomainEntity(saved);
    }

    @Override
    public void delete(Duty duty) {
        DutyJpaEntity entity = dutyMapper.mapToJpaEntity(duty);
        dutyRepository.delete(entity);
    }

    @Override
    public Optional<Duty> findById(Long dutyId) {
        return dutyRepository.findById(dutyId)
                .map(dutyMapper::mapToDomainEntity);
    }

    @Override
    public Optional<Duty> findByIdAndPlaceId(Long dutyId, Long placeId) {
        return dutyRepository.findByDutyIdAndPlace_PlaceId(dutyId, placeId)
                .map(dutyMapper::mapToDomainEntity);
    }

    @Override
    public List<Duty> findByPlaceId(Long placeId) {
        return dutyRepository.findByPlace_PlaceId(placeId).stream()
                .map(dutyMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public boolean existsByNameAndPlaceId(String name, Long placeId) {
        return dutyRepository.existsByNameAndPlace_PlaceId(name, placeId);
    }

    @Override
    public String getDutyNameById(Long dutyId) {
        DutyJpaEntity duty = dutyRepository.findById(dutyId).get();
        return duty.getName();
    }

    @Override
    public List<Duty> findAll() {
        List<DutyJpaEntity> all = dutyRepository.findAll();
        List<Duty> duties = new ArrayList<>();

        for (DutyJpaEntity jpaEntity : all) {
            duties.add(dutyMapper.mapToDomainEntity(jpaEntity));
        }

        return duties;
    }

    @Override
    public List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds) {
        List<DutyJpaEntity> dutyJpaEntities = memberDutyRepository.findDistinctDutiesByMemberIds(memberIds);
        return dutyJpaEntities.stream().map(dutyMapper::mapToDomainEntity).toList();
    }
}
