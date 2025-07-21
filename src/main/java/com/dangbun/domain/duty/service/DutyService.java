package com.dangbun.domain.duty.service;

import com.dangbun.domain.duty.dto.request.PostDutyCreateRequest;
import com.dangbun.domain.duty.dto.request.PutDutyUpdateRequest;
import com.dangbun.domain.duty.dto.response.GetDutyListResponse;
import com.dangbun.domain.duty.dto.response.PostDutyCreateResponse;
import com.dangbun.domain.duty.dto.response.PutDutyUpdateResponse;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.exception.custom.*;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.repository.PlaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.dangbun.domain.duty.response.status.DutyExceptionResponse.*;

@Service
@RequiredArgsConstructor
public class DutyService {

    private final DutyRepository dutyRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public PostDutyCreateResponse createDuty(Long placeId, PostDutyCreateRequest request) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new PlaceNotFoundException(PLACE_NOT_FOUND));

        if (dutyRepository.existsByNameAndPlace_PlaceId(request.name(), placeId)) {
            throw new DutyAlreadyExistsException(DUTY_ALREADY_EXISTS);
        }

        Duty duty = Duty.builder()
                .name(request.name())
                .icon(request.icon())
                .place(place)
                .build();

        Duty saved = dutyRepository.save(duty);
        return PostDutyCreateResponse.of(saved.getDutyId());
    }

    @Transactional
    public List<GetDutyListResponse> getDutyList(Long placeId) {
        if (!placeRepository.existsById(placeId)) {
            throw new PlaceNotFoundException(PLACE_NOT_FOUND);
        }

        List<Duty> duties = dutyRepository.findByPlace_PlaceId(placeId);

        return duties.stream()
                .map(GetDutyListResponse::of)
                .toList();
    }

    @Transactional
    public PutDutyUpdateResponse updateDuty(Long dutyId, PutDutyUpdateRequest request) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        duty.update(request.name(), request.icon());

        return PutDutyUpdateResponse.of(duty.getDutyId(),duty.getName(), duty.getIcon());
    }

}
