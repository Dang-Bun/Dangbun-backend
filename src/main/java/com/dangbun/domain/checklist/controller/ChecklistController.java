package com.dangbun.domain.checklist.controller;

import com.dangbun.domain.member.response.status.MemberExceptionResponse;
import com.dangbun.global.aop.CheckChecklistMembership;
import com.dangbun.domain.checklist.dto.request.PostGetPresignedUrlRequest;
import com.dangbun.domain.checklist.dto.request.PostSaveUploadResultRequest;
import com.dangbun.domain.checklist.dto.response.GetImageUrlResponse;
import com.dangbun.domain.checklist.dto.response.PostCompleteChecklistResponse;
import com.dangbun.domain.checklist.dto.response.PostGetPresignedUrlResponse;
import com.dangbun.domain.checklist.dto.response.PostIncompleteChecklistResponse;
import com.dangbun.domain.checklist.response.status.ChecklistExceptionResponse;
import com.dangbun.domain.checklist.service.ChecklistService;
import com.dangbun.domain.cleaningImage.response.status.CleaningImageExceptionResponse;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@CheckPlaceMembership()
@CheckChecklistMembership()
@Tag(name = "Checklist", description = "ChecklistController - 체크리스트 관련 API")
@RequestMapping("/places/{placeId}/checklists/{checklistId}")
public class ChecklistController {

    private final ChecklistService checklistService;


    @Operation(summary = "체크리스트 완료")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, ChecklistExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "CHECKLIST_ACCESS_DENIED", "ALREADY_CHECKED"}
    )
    @PostMapping("/actions/complete")
    public ResponseEntity<BaseResponse<PostCompleteChecklistResponse>> completeChecklist(@PathVariable("placeId") Long placeId,
                                                                                         @PathVariable("checklistId") Long checklistId) {
        return ResponseEntity.ok(BaseResponse.ok(checklistService.completeChecklist()));
    }

    @Operation(summary = "체크리스트 해제")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class,ChecklistExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "CHECKLIST_ACCESS_DENIED","ALREADY_UNCHECKED"}
    )
    @PostMapping("/actions/incomplete")
    public ResponseEntity<BaseResponse<PostIncompleteChecklistResponse>> incompleteChecklist(@PathVariable("placeId") Long placeId,
                                                                                             @PathVariable("checklistId") Long checklistId) {
        return ResponseEntity.ok(BaseResponse.ok(checklistService.incompleteChecklist()));
    }

    @Operation(summary = "이미지 등록 url 생성", description = "s3 이미지 업로드 url을 획득합니다.")
    @PostMapping("/photos/upload-url")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, ChecklistExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "CHECKLIST_ACCESS_DENIED", "CLEANING_IMAGE_ALREADY_EXISTS"}
    )
    public ResponseEntity<BaseResponse<PostGetPresignedUrlResponse>> getPresignedUrl(@PathVariable("placeId") Long placeId,
                                                                                     @PathVariable("checklistId") Long checklistId,
                                                                                     @RequestBody PostGetPresignedUrlRequest request) {

        return ResponseEntity.ok(BaseResponse.ok(checklistService.generateImageUrl(request)));
    }

    @Operation(summary = "이미지 등록 성공", description = "FE에서 이미지 업로드를 완료 후 BE에 알리는 용도")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, ChecklistExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "CHECKLIST_ACCESS_DENIED", "INVALID_S3_KEY"}
    )
    @PostMapping("/photos/complete")
    public ResponseEntity<?> saveUploadResult(@PathVariable("placeId") Long placeId,
                                              @PathVariable("checklistId") Long checklistId,
                                              @RequestBody PostSaveUploadResultRequest request) {
        checklistService.saveUploadResult(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "이미지 확인 url 요청", description = "이미지 확인을 위한 url을 요청합니다.")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, CleaningImageExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "CHECKLIST_ACCESS_DENIED", "NO_SUCH_IMAGE"}
    )
    @GetMapping("/photos")
    public ResponseEntity<BaseResponse<GetImageUrlResponse>> getImageUrl(@PathVariable("placeId") Long placeId,
                                                                         @PathVariable("checklistId") Long checklistId) {

        return ResponseEntity.ok(BaseResponse.ok(checklistService.getImageUrl(checklistId)));
    }


}
