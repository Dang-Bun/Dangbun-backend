package com.dangbun.domain.checklist.controller;

import com.dangbun.domain.checklist.CheckChecklistMembership;
import com.dangbun.domain.checklist.service.ChecklistService;
import com.dangbun.domain.member.CheckPlaceMembership;
import com.dangbun.global.response.BaseResponse;
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


    @PostMapping("/complete")
    public ResponseEntity<?> completeChecklist(@PathVariable("placeId") Long placeId,
                                           @PathVariable("checklistId") Long checklistId){
        checklistService.completeChecklist(placeId, checklistId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

//    @PostMapping("/checkList/{checkListId}")

}
