package com.dangbun.domain.duty.service;

import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import com.dangbun.domain.cleaning.service.CleaningService;
import com.dangbun.domain.cleaningdate.repository.CleaningDateRepository;
import com.dangbun.domain.duty.dto.request.*;
import com.dangbun.domain.duty.dto.response.*;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.exception.custom.*;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.repository.PlaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.dangbun.domain.duty.response.status.DutyExceptionResponse.*;

@Service
@RequiredArgsConstructor
public class DutyService {

    private final DutyRepository dutyRepository;
    private final PlaceRepository placeRepository;
    private final MemberDutyRepository memberDutyRepository;
    private final CleaningRepository cleaningRepository;
    private final MemberRepository memberRepository;
    private final CleaningDateRepository cleaningDateRepository;
    private final MemberCleaningRepository memberCleaningRepository;
    private final CleaningService cleaningService;

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

    @Transactional
    public void deleteDuty(Long dutyId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        List<Cleaning> cleanings = cleaningRepository.findAllByDuty(duty);
        cleaningDateRepository.deleteAllByCleaningIn(cleanings);

        for(Cleaning cleaning : cleanings) {
            cleaningService.deleteCleaning(cleaning.getCleaningId());
        }
        dutyRepository.delete(duty);
    }

    public List<GetDutyMemberNameListResponse> getDutyMemberNameList(Long dutyId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        List<MemberDuty> members = memberDutyRepository.findAllByDuty(duty);

        return members.stream()
                .map(md -> GetDutyMemberNameListResponse.of(md.getMember()))
                .toList();
    }

    public List<GetDutyCleaningNameListResponse> getDutyCleaningNameList(Long dutyId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        List<Cleaning> cleanings = cleaningRepository.findAllByDuty(duty);

        return cleanings.stream()
                .map(GetDutyCleaningNameListResponse::of)
                .toList();
    }


    @Transactional
    public PostAddMembersResponse addMembers(Long dutyId, PostAddMembersRequest request) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        List<Long> requestedIds = request.memberIds();

        List<Member> members = memberRepository.findAllById(requestedIds);
        Set<Long> foundIds = members.stream()
                .map(Member::getMemberId)
                .collect(Collectors.toSet());

        List<Long> notFoundIds = requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!notFoundIds.isEmpty()) {
            throw new MemberNotFoundException(MEMBER_NOT_FOUND);
        }


        List<Long> addedMemberIds = new ArrayList<>();
        for (Member member : members) {
            boolean exists = memberDutyRepository.existsByDutyAndMember(duty, member);
            if (!exists) {
                MemberDuty md = MemberDuty.builder()
                        .duty(duty)
                        .member(member)
                        .build();
                memberDutyRepository.save(md);
                addedMemberIds.add(member.getMemberId());
            }
        }

        return PostAddMembersResponse.of(addedMemberIds);
    }

    @Transactional
    public void assignMember(Long dutyId, PatchAssignMemberRequest request) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        List<Cleaning> cleanings = cleaningRepository.findAllByDuty(duty);
        List<Member> allMembers = memberDutyRepository.findMembersByDuty(duty);

        switch (request.assignType()) {

            case CUSTOM -> {
                Cleaning cleaning = cleaningRepository.findByCleaningIdAndDuty_DutyId(request.cleaningId(), dutyId)
                        .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

                List<Member> selectedMembers = memberRepository.findAllById(request.memberIds());
                cleaning.updateMembers(selectedMembers);
                cleaningRepository.save(cleaning);
            }

            case COMMON -> {
                if (allMembers.isEmpty()) {
                    throw new MemberNotExistsException(MEMBER_NOT_EXISTS);
                }
                for (Cleaning cleaning : cleanings) {
                    cleaning.updateMembers(allMembers);
                }
                cleaningRepository.saveAll(cleanings);
            }

            case RANDOM -> {
                Random random = new Random();
                for (Cleaning cleaning : cleanings) {

                    List<Member> shuffled = new ArrayList<>(allMembers);
                    Collections.shuffle(shuffled, random);
                    List<Member> assigned = shuffled.stream()
                            .limit(request.assignCount())
                            .toList();
                    cleaning.updateMembers(assigned);
                }
                cleaningRepository.saveAll(cleanings);
            }
        }
    }

    @Transactional
    public List<GetCleaningInfoListResponse> getCleaningInfoList(Long dutyId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        List<Cleaning> cleanings = cleaningRepository.findAllByDuty(duty);
        int dutyMemberCount = memberDutyRepository.findMembersByDuty(duty).size();

        return cleanings.stream()
                .map(cleaning -> {
                    List<MemberCleaning> mappings = memberCleaningRepository.findAllByCleaning(cleaning);
                    List<Member> members = mappings.stream()
                            .map(MemberCleaning::getMember)
                            .toList();

                    List<String> displayednames = members.stream()
                            .map(Member::getName)
                            .limit(2)
                            .toList();

                    boolean isCommon = members.size() == dutyMemberCount;

                    return GetCleaningInfoListResponse.of(
                            cleaning.getCleaningId(),
                            cleaning.getName(),
                            displayednames,
                            members.size()
                    );
                })
                .toList();
    }

    @Transactional
    public PostAddCleaningsResponse addCleanings(Long dutyId, PostAddCleaningsRequest request) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        List<Cleaning> cleanings = cleaningRepository.findAllById(request.cleaningIds());
        List<Long> assignedIds = new ArrayList<>();

        for (Cleaning cleaning : cleanings) {
            if (cleaning.getDuty() == null) {
                cleaning.assignToDuty(duty);
                assignedIds.add(cleaning.getCleaningId());
            }
        }
        cleaningRepository.saveAll(cleanings);



        return PostAddCleaningsResponse.of(assignedIds);
    }

    @Transactional
    public void removeCleaningFromDuty(Long dutyId, Long cleaningId) {
        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotFoundException(DUTY_NOT_FOUND));

        Cleaning cleaning = cleaningRepository.findById(cleaningId)
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        if (cleaning.getDuty() == null || !cleaning.getDuty().getDutyId().equals(dutyId)) {
            throw new CleaningNotAssignedException(CLEANING_NOT_ASSIGNED);
        }

        cleaning.removeDuty();



    }


}
