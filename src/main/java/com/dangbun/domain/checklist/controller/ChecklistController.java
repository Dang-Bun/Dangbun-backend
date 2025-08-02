package com.dangbun.domain.checklist.controller;

import com.dangbun.domain.checklist.CheckChecklistMembership;
import com.dangbun.domain.checklist.response.status.ChecklistExceptionResponse;
import com.dangbun.domain.checklist.service.ChecklistService;
import com.dangbun.domain.member.CheckPlaceMembership;
import com.dangbun.domain.member.response.status.MemberExceptionResponse;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@CheckPlaceMembership(placeIdParam = "placeId")
@CheckChecklistMembership(checklistIdParam = "checklistId")
@Tag(name = "Checklist", description = "ChecklistController - 체크리스트 관련 API")
@RequestMapping("/places/{placeId}/checkLists/{checklistId}")
public class ChecklistController {

    private final ChecklistService checklistService;


    @Operation(summary = "체크리스트 완료")
    @DocumentedApiErrors(
            value = {ChecklistExceptionResponse.class},
            includes = {"ALREADY_CHECKED"}
    )
    @PostMapping("/actions/complete")
    public ResponseEntity<?> completeChecklist(@PathVariable("placeId") Long placeId,
                                           @PathVariable("checklistId") Long checklistId){
        checklistService.completeChecklist();
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "체크리스트 해제")
    @DocumentedApiErrors(
            value = {ChecklistExceptionResponse.class},
            includes = {"ALREADY_UNCHECKED"}
    )
    @PostMapping("/actions/incomplete")
    public ResponseEntity<?> incompleteChecklist(@PathVariable("placeId") Long placeId,
                                                 @PathVariable("checklistId") Long checklistId){
        checklistService.incompleteChecklist();
        return ResponseEntity.ok(BaseResponse.ok(null));
    }


}
