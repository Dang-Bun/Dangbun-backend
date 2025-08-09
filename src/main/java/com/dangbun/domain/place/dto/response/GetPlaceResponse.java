package com.dangbun.domain.place.dto.response;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.entity.PlaceCategory;
import com.dangbun.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Schema(description = "아직 초대받지 않은 유저일 경우 본인 맴버 ID, 플레이스 ID, 플레이스 이름만 반환됨 (나머지 필드는 NULL)")
public record GetPlaceResponse(
        @Schema(description = "본인 맴버 ID", example = "1")
        Long memberId,

        @Schema(description = "플레이스 ID", example = "1")
        Long placeId,

        @Schema(description = "플레이스 이름", example = "메가박스")
        String placeName,

        @Schema(description = "플레이스 카테고리(CAFE, RESTAURANT, THEATER, DOMITORY, BUILDING, OFFICE, SCHOOL, GYM, ETC)", example = "CAFE")
        PlaceCategory category,

        @Schema(description = "청소 마감 시간", example = "23:59")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime endTime,

        @Schema(description = "당번 리스트")
        List<DutyDto> duties
) {

    public static GetPlaceResponse of(User user, Place place, Map<MemberDuty, List<Checklist>> cleaningMap, List<MemberCleaning> memberCleanings) {

        MemberDuty memberDuty = cleaningMap.keySet().stream().filter(md -> md.getMember().getUser().getUserId().equals(user.getUserId()))
                .findAny().orElseThrow(() -> new RuntimeException("GetPlaceResponse 에러"));

        Member member = memberDuty.getMember();
        Long placeId = place.getPlaceId();
        String placeName = place.getName();
        LocalTime endTime = place.getEndTime();

        List<DutyDto> duties = new ArrayList<>();
        for (Map.Entry<MemberDuty, List<Checklist>> mdc : cleaningMap.entrySet()) {
            MemberDuty md = mdc.getKey();
            List<CheckListDto> checkListDtos = new ArrayList<>();
            for (Checklist checkList : mdc.getValue()) {

                Cleaning cleaning = checkList.getCleaning();

                List<Member> members = new ArrayList<>();
                for (MemberCleaning mc : memberCleanings) {
                    if (mc.getCleaning().equals(cleaning)) {
                        members.add(mc.getMember());
                    }
                }
                checkListDtos.add(CheckListDto.of(checkList, members));
            }

            duties.add(DutyDto.of(md.getDuty().getName(), checkListDtos));
        }

        return new GetPlaceResponse(member.getMemberId(),
                placeId,
                placeName,
                place.getCategory(),
                endTime,
                duties);
    }


    @Schema(name = "GetPlaceResponse.DutyDto", description = "당번 DTO")
    public record DutyDto(
            @Schema(description = "당번 이름", example = "로비 청소")
            String dutyName,

            @Schema(description = "청소 리스트")
            List<CheckListDto> checkLists
    ) {
        public static DutyDto of(String dutyName, List<CheckListDto> checkLists) {
            return new DutyDto(dutyName, checkLists);
        }
    }

    @Schema(name = "GetPlaceResponse.CheckListDto", description = "청소 DTO")
    public record CheckListDto(
            @Schema(description = "체크리스트 ID", example = "1")
            Long checkListId,

            @Schema(description = "청소 담당 맴버")
            List<MemberDto> members,

            @Schema(description = "청소 이름", example = "복도 바닥 쓸기")
            String cleaningName,

            @Schema(description = "청소 완료 시간(null일 시 미완료)", example = "23:47")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime completeTime,

            @Schema(description = "사진 필요 여부", example = "false")
            Boolean needPhoto

    ) {
        public static CheckListDto of(Checklist checkList, List<Member> members) {

            List<MemberDto> memberDtos = members.stream()
                    .map(MemberDto::of).toList();
            return new CheckListDto(checkList.getChecklistId(), memberDtos, checkList.getCleaning().getName(), null, checkList.getCleaning().getNeedPhoto());
        }
    }

    @Schema(name = "GetPlaceResponse.MemberDto", description = "맴버 DTO")
    public record MemberDto(
            @Schema(description = "청소 담당 맴버 ID", example = "1")
            Long memberId,

            @Schema(description = "청소 담당 맴버 이름", example = "맴버 A")
            String memberName
    ) {
        public static MemberDto of(Member member) {
            return new MemberDto(member.getMemberId(), member.getName());
        }
    }
}
