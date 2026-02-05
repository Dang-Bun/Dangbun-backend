package com.dangbun.domain.cleaning.refactor.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.cleaning.refactor.adapter.out.CleaningJpaEntity;
import com.dangbun.domain.cleaning.refactor.application.port.out.CleaningCommandPort;
import com.dangbun.domain.cleaning.refactor.application.port.out.CleaningQueryPort;
import com.dangbun.domain.cleaning.refactor.domain.Cleaning;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
class CleaningPersistenceAdapter implements CleaningCommandPort, CleaningQueryPort {

    private final CleaningRepository cleaningRepository;
    private final CleaningMapper cleaningMapper;

    @Override
    public Cleaning save(Cleaning cleaning) {
        CleaningJpaEntity entity = cleaningMapper.mapToJpaEntity(cleaning);
        CleaningJpaEntity saved = cleaningRepository.save(entity);
        return cleaningMapper.mapToDomainEntity(saved);
    }

    @Override
    public void saveAll(List<Cleaning> cleanings) {
        List<CleaningJpaEntity> entities = cleanings.stream()
                .map(cleaningMapper::mapToJpaEntity)
                .toList();
        cleaningRepository.saveAll(entities);
    }

    @Override
    public void delete(Cleaning cleaning) {
        CleaningJpaEntity entity = cleaningMapper.mapToJpaEntity(cleaning);
        cleaningRepository.delete(entity);
    }

    @Override
    public void deleteById(Long cleaningId) {
        cleaningRepository.deleteById(cleaningId);
    }

    @Override
    public Optional<Cleaning> findById(Long cleaningId) {
        return cleaningRepository.findById(cleaningId)
                .map(cleaningMapper::mapToDomainEntity);
    }

    @Override
    public Optional<Cleaning> findWithDutyNullableById(Long cleaningId) {
        return cleaningRepository.findWithDutyNullableById(cleaningId)
                .map(cleaningMapper::mapToDomainEntity);
    }

    @Override
    public Optional<Cleaning> findByCleaningIdAndDutyId(Long cleaningId, Long dutyId) {
        return cleaningRepository.findByCleaningIdAndDuty_DutyId(cleaningId, dutyId)
                .map(cleaningMapper::mapToDomainEntity);
    }

    @Override
    public List<Cleaning> findAllByDutyId(Long dutyId) {
        /*
         * TODO: DutyJpaEntity 의존성 제거 필요
         * 현재 CleaningRepository.findAllByDuty()가 DutyJpaEntity를 파라미터로 받음
         * dutyId 기반 쿼리 메서드 추가 필요
         */
        return cleaningRepository.findAll().stream()
                .filter(c -> c.getDuty() != null && c.getDuty().getDutyId().equals(dutyId))
                .map(cleaningMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public List<Cleaning> findByDutyIdAndMemberIds(Long dutyId, List<Long> memberIds) {
        return cleaningRepository.findByDutyIdAndMemberIdsWithMembersJoin(dutyId, memberIds).stream()
                .map(cleaningMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public List<Cleaning> findUnassignedCleaningsByPlaceId(Long placeId) {
        return cleaningRepository.findUnassignedCleaningsByPlaceId(placeId).stream()
                .map(cleaningMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public List<Cleaning> findByPlaceId(Long placeId) {
        /*
         * TODO: Place 의존성 제거 필요
         * 현재 CleaningRepository.findByPlace()가 Place 엔티티를 파라미터로 받음
         * placeId 기반 쿼리 메서드 추가 필요
         */
        return cleaningRepository.findAll().stream()
                .filter(c -> c.getPlace().getPlaceId().equals(placeId))
                .map(cleaningMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public boolean existsByNameAndDutyIdAndPlaceId(String name, Long dutyId, Long placeId) {

        return cleaningRepository.existsByNameAndDuty_DutyIdAndPlace_PlaceId(name, dutyId, placeId);
    }

    @Override
    public boolean existsByNameAndDutyIdAndCleaningIdNotAndPlaceId(String name, Long dutyId, Long cleaningId, Long placeId) {


        return cleaningRepository.existsByNameAndDuty_DutyIdAndCleaningIdNotAndPlace_PlaceId(
                name,
                dutyId,
                cleaningId,
                placeId);
    }
}
