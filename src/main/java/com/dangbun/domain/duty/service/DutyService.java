package com.dangbun.domain.duty.service;

import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaning.exception.custom.DutyNotFoundException;
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
import com.dangbun.global.context.DutyContext;
import com.dangbun.global.context.MemberContext;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.dangbun.domain.duty.response.status.DutyExceptionResponse.*;

@Service
@RequiredArgsConstructor
@Transactional
public class DutyService {

    private final DutyRepository dutyRepository;
    private final MemberDutyRepository memberDutyRepository;
    private final CleaningRepository cleaningRepository;
    private final MemberRepository memberRepository;
    private final CleaningDateRepository cleaningDateRepository;
    private final MemberCleaningRepository memberCleaningRepository;
    private final CleaningService cleaningService;


    public PostDutyCreateResponse createDuty(PostDutyCreateRequest request) {

        Place place = MemberContext.get().getPlace();

        if (dutyRepository.existsByNameAndPlace_PlaceId(request.name(), place.getPlaceId())) {
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

    @Transactional(readOnly = true)
    public List<GetDutyListResponse> getDutyList() {
        Long placeId = MemberContext.get().getPlace().getPlaceId();

        List<Duty> duties = dutyRepository.findByPlace_PlaceId(placeId);

        return duties.stream()
                .map(GetDutyListResponse::of)
                .toList();
    }


    public PutDutyUpdateResponse updateDuty( PutDutyUpdateRequest request) {
        Duty duty = DutyContext.get();

        duty.update(request.name(), request.icon());

        return PutDutyUpdateResponse.of(duty.getDutyId(),duty.getName(), duty.getIcon());
    }


    public void deleteDuty(Long dutyId) {
        //Duty duty = DutyContext.get();

        Duty duty = dutyRepository.findById(dutyId)
                .orElseThrow(() -> new DutyNotInPlaceFoundException(DUTY_NOT_IN_PLACE));

        List<Cleaning> cleanings = cleaningRepository.findAllByDuty(duty);
        cleaningDateRepository.deleteAllByCleaningIn(cleanings);

        for(Cleaning cleaning : cleanings) {
            cleaningService.deleteCleaning(cleaning.getCleaningId());
        }
        dutyRepository.delete(duty);
    }

    @Transactional(readOnly = true)
    public List<GetDutyMemberNameListResponse> getDutyMemberNameList() {
        Duty duty = DutyContext.get();
        List<MemberDuty> members = memberDutyRepository.findAllByDuty(duty);

        return members.stream()
                .map(md -> GetDutyMemberNameListResponse.of(md.getMember()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GetDutyCleaningNameListResponse> getDutyCleaningNameList() {
        Duty duty = DutyContext.get();

        List<Cleaning> cleanings = cleaningRepository.findAllByDuty(duty);

        return cleanings.stream()
                .map(GetDutyCleaningNameListResponse::of)
                .toList();
    }



    public PostAddMembersResponse addMembers(PostAddMembersRequest request) {
        Duty duty = DutyContext.get();

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


    public void assignMember( PatchAssignMemberRequest request) {
        Duty duty = DutyContext.get();

        List<Cleaning> cleanings = cleaningRepository.findAllByDuty(duty);
        List<Member> allMembers = memberDutyRepository.findMembersByDuty(duty);

        switch (request.assignType()) {

            case CUSTOM -> {
                Cleaning cleaning = cleaningRepository.findByCleaningIdAndDuty_DutyId(request.cleaningId(), duty.getDutyId())
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

    @Transactional(readOnly = true)
    public List<GetCleaningInfoListResponse> getCleaningInfoList() {
        Duty duty = DutyContext.get();

        List<Cleaning> cleanings = cleaningRepository.findAllByDuty(duty);

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

                    return GetCleaningInfoListResponse.of(
                            cleaning.getCleaningId(),
                            cleaning.getName(),
                            displayednames,
                            members.size()
                    );
                })
                .toList();
    }


    public PostAddCleaningsResponse addCleanings(PostAddCleaningsRequest request) {
        Duty duty = DutyContext.get();

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


    public void removeCleaningFromDuty( Long cleaningId) {
        Long dutyId = DutyContext.get().getDutyId();

        Cleaning cleaning = cleaningRepository.findById(cleaningId)
                .orElseThrow(() -> new CleaningNotFoundException(CLEANING_NOT_FOUND));

        if (cleaning.getDuty() == null || !cleaning.getDuty().getDutyId().equals(dutyId)) {
            throw new CleaningNotAssignedException(CLEANING_NOT_ASSIGNED);
        }

        cleaning.removeDuty();

    }


}
