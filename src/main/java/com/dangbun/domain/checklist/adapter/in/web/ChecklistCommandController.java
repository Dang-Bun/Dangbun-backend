package com.dangbun.domain.checklist.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.checklist.adapter.in.web.dto.request.PostGetPresignedUrlRequest;
import com.dangbun.domain.checklist.adapter.in.web.dto.request.PostSaveUploadResultRequest;
import com.dangbun.domain.checklist.adapter.in.web.dto.response.PostCompleteChecklistResponse;
import com.dangbun.domain.checklist.adapter.in.web.dto.response.PostGetPresignedUrlResponse;
import com.dangbun.domain.checklist.adapter.in.web.dto.response.PostIncompleteChecklistResponse;
import com.dangbun.domain.checklist.application.port.in.command.ChecklistCommandUseCase;
import com.dangbun.domain.checklist.response.status.ChecklistExceptionResponse;
import com.dangbun.domain.member.exception.status.MemberExceptionResponse;
import com.dangbun.global.aop.CheckChecklistMembership;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Checklist", description = "ChecklistController - 체크리스트 관련 API")
@CheckPlaceMembership()
@CheckChecklistMembership()
@RequiredArgsConstructor
@WebAdapter(path = "/places/{placeId}/checklists/{checklistId}")
public class ChecklistCommandController {

    private final ChecklistCommandUseCase checklistCommandUseCase;

    @Operation(summary = "체크리스트 완료")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, ChecklistExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "CHECKLIST_ACCESS_DENIED", "ALREADY_CHECKED"}
    )
    @PostMapping("/actions/complete")
    public ResponseEntity<BaseResponse<PostCompleteChecklistResponse>> completeChecklist(
            @PathVariable Long placeId,
            @PathVariable Long checklistId
    ) {
        return ResponseEntity.ok(BaseResponse.ok(checklistCommandUseCase.completeChecklist()));
    }

    @Operation(summary = "체크리스트 해제")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, ChecklistExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "CHECKLIST_ACCESS_DENIED", "ALREADY_UNCHECKED"}
    )
    @PostMapping("/actions/incomplete")
    public ResponseEntity<BaseResponse<PostIncompleteChecklistResponse>> incompleteChecklist(
            @PathVariable Long placeId,
            @PathVariable Long checklistId
    ) {
        return ResponseEntity.ok(BaseResponse.ok(checklistCommandUseCase.incompleteChecklist()));
    }

    @Operation(summary = "이미지 등록 url 생성", description = "s3 이미지 업로드 url을 획득합니다.")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, ChecklistExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "CHECKLIST_ACCESS_DENIED", "CLEANING_IMAGE_ALREADY_EXISTS"}
    )
    @PostMapping("/photos/upload-url")
    public ResponseEntity<BaseResponse<PostGetPresignedUrlResponse>> getPresignedUrl(
            @PathVariable Long placeId,
            @PathVariable Long checklistId,
            @RequestBody PostGetPresignedUrlRequest request
    ) {
        return ResponseEntity.ok(BaseResponse.ok(checklistCommandUseCase.generateImageUrl(request)));
    }

    @Operation(summary = "이미지 등록 성공", description = "FE에서 이미지 업로드를 완료 후 BE에 알리는 용도")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, ChecklistExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "CHECKLIST_ACCESS_DENIED", "INVALID_S3_KEY"}
    )
    @PostMapping("/photos/complete")
    public ResponseEntity<BaseResponse<Void>> saveUploadResult(
            @PathVariable Long placeId,
            @PathVariable Long checklistId,
            @RequestBody PostSaveUploadResultRequest request
    ) {
        checklistCommandUseCase.saveUploadResult(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "이미지 삭제", description = "이미지 확인용 s3Key를 DB에서 삭제합니다.")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, ChecklistExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "CHECKLIST_ACCESS_DENIED"}
    )
    @DeleteMapping("/photos")
    public ResponseEntity<BaseResponse<Void>> deleteImage(
            @PathVariable Long placeId,
            @PathVariable Long checklistId
    ) {
        checklistCommandUseCase.deleteImage(checklistId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
