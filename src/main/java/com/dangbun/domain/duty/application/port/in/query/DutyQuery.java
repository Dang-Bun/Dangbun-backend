package com.dangbun.domain.duty.application.port.in.query;

public interface DutyQuery {

    DutyListResult getDutyList(Long placeId);

    DutyMembersResult getDutyMembers(Long dutyId);

    DutyCleaningsResult getDutyCleanings(Long dutyId);

    CleaningInfoListResult getCleaningInfoList(Long dutyId);
}
