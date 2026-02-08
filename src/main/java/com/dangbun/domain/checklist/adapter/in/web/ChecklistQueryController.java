package com.dangbun.domain.checklist.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.checklist.adapter.in.web.dto.response.GetImageUrlResponse;
import com.dangbun.domain.checklist.application.port.in.query.ChecklistQuery;
import com.dangbun.domain.cleaningImage.exception.status.CleaningImageExceptionResponse;
import com.dangbun.domain.member.exception.status.MemberExceptionResponse;
import com.dangbun.global.aop.CheckChecklistMembership;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Checklist", description = "ChecklistController - 체크리스트 관련 API")
@CheckPlaceMembership()
@CheckChecklistMembership()
@RequiredArgsConstructor
@WebAdapter(path = "/places/{placeId}/checklists/{checklistId}")
public class ChecklistQueryController {

    private final ChecklistQuery checklistQuery;

    @Operation(summary = "이미지 확인 url 요청", description = "이미지 확인을 위한 url을 요청합니다.")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, CleaningImageExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "CHECKLIST_ACCESS_DENIED", "NO_SUCH_IMAGE"}
    )
    @GetMapping("/photos")
    public ResponseEntity<BaseResponse<GetImageUrlResponse>> getImageUrl(
            @PathVariable Long placeId,
            @PathVariable Long checklistId
    ) {
        return ResponseEntity.ok(BaseResponse.ok(checklistQuery.getImageUrl(checklistId)));
    }
}
