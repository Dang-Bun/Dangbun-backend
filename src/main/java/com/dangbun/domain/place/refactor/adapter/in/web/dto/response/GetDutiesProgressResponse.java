package com.dangbun.domain.place.refactor.adapter.in.web.dto.response;

import java.util.List;

public record GetDutiesProgressResponse(
        List<DutyProgressDto> dutyProgressDtos
) {
    public static GetDutiesProgressResponse of(List<DutyProgressDto> dutyProgressDtos){
        return new GetDutiesProgressResponse(dutyProgressDtos);
    }
}
