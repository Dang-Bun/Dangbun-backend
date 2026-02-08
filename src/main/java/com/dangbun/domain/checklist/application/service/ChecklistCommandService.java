package com.dangbun.domain.checklist.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.checklist.application.port.out.ChecklistCommandPort;
import com.dangbun.domain.checklist.application.port.out.ChecklistQueryPort;
import com.dangbun.domain.checklist.domain.Checklist;
import com.dangbun.domain.checklist.exception.custom.ChecklistRequireImageException;
import com.dangbun.domain.checklist.adapter.in.web.dto.request.PostGetPresignedUrlRequest;
import com.dangbun.domain.checklist.adapter.in.web.dto.request.PostSaveUploadResultRequest;
import com.dangbun.domain.checklist.adapter.in.web.dto.response.PostCompleteChecklistResponse;
import com.dangbun.domain.checklist.adapter.in.web.dto.response.PostGetPresignedUrlResponse;
import com.dangbun.domain.checklist.adapter.in.web.dto.response.PostIncompleteChecklistResponse;
import com.dangbun.domain.checklist.adapter.out.persistence.ChecklistJpaEntity;
import com.dangbun.domain.checklist.application.port.in.command.ChecklistCommandUseCase;
import com.dangbun.domain.checklist.application.port.in.command.ChecklistForCalendarUseCase;
import com.dangbun.domain.checklist.application.port.in.command.CreateChecklistByDateAndTimeUseCase;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningForChecklistQuery;
import com.dangbun.domain.cleaningdate.domain.CleaningDate;
import com.dangbun.domain.cleaningImage.application.port.in.command.CleaningImageCommandUseCase;
import com.dangbun.domain.cleaningImage.application.port.in.query.CleaningImageQuery;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.membercleaning.application.port.in.query.GetMembersByCleaningQuery;
import com.dangbun.domain.place.application.port.in.query.GetPlaceEndTimeQuery;
import com.dangbun.global.context.ChecklistContext;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.dangbun.domain.checklist.response.status.ChecklistExceptionResponse.REQUIRE_IMAGE;

@UseCase
@RequiredArgsConstructor
@Transactional
public class ChecklistCommandService implements ChecklistCommandUseCase, CreateChecklistByDateAndTimeUseCase, ChecklistForCalendarUseCase {

    private final CleaningImageCommandUseCase cleaningImageCommandUseCase;
    private final CleaningImageQuery cleaningImageQuery;
    private final GetMembersByCleaningQuery getMembersByCleaningQuery;
    private final GetPlaceEndTimeQuery getPlaceEndTimeQuery;

    private final ChecklistQueryPort checklistQueryPort;
    private final ChecklistCommandPort checklistCommandPort;

    private final GetCleaningForChecklistQuery getCleaningForChecklistQuery;


    @Override
    public PostCompleteChecklistResponse completeChecklist() {
        MemberJpaEntity member = MemberContext.get();
        Checklist checklist = checklistQueryPort.findById(ChecklistContext.get().getChecklistId()).get();

        if (isRequiredImage(checklist)) {
            checkIsImageRegistered(checklist);
        }

        Checklist completed = checklistCommandPort.completeChecklist(checklist.getChecklistId().value(), member.getMemberId());
        LocalDateTime endTime = completed.getUpdatedAt();

        return PostCompleteChecklistResponse.of(member.getName(), LocalTime.from(endTime));
    }

    @Override
    public PostIncompleteChecklistResponse incompleteChecklist() {
        Checklist checklist = checklistQueryPort.findById(ChecklistContext.get().getChecklistId()).get();

        GetCleaningForChecklistQuery.CleaningInfo cleaningInfo = getCleaningForChecklistQuery
                .findById(checklist.getCleaningId())
                .orElseThrow(() -> new IllegalStateException("Cleaning not found for checklist"));

        List<Member> members = getMembersByCleaningQuery.getMembersByCleaningId(cleaningInfo.cleaningId());
        List<String> membersName = members.stream().map(Member::getName).toList();
        LocalTime endTime = getPlaceEndTimeQuery.getEndTimeByPlaceId(cleaningInfo.placeId());

        Checklist incompleted = checklistCommandPort.incompleteChecklist(checklist.getChecklistId().value());

        return PostIncompleteChecklistResponse.of(incompleted.getChecklistId().value(), membersName, endTime);
    }

    @Override
    public PostGetPresignedUrlResponse generateImageUrl(PostGetPresignedUrlRequest request) {
        ChecklistJpaEntity checklistJpaEntity = ChecklistContext.get();
        Map<String, String> uploadUrlAndKey = cleaningImageCommandUseCase
                .generateUploadUrl(request.originalFileName(), request.contentType(), checklistJpaEntity.getChecklistId());

        return new PostGetPresignedUrlResponse(uploadUrlAndKey.get("uploadUrl"), uploadUrlAndKey.get("s3Key"));
    }

    @Override
    public void saveUploadResult(PostSaveUploadResultRequest request) {
        ChecklistJpaEntity checklistJpaEntity = ChecklistContext.get();
        cleaningImageCommandUseCase.saveImage(checklistJpaEntity.getChecklistId(), request.s3Key());
    }

    @Override
    public void deleteImage(Long checklistId) {
        if (cleaningImageQuery.isImagePresent(checklistId)) {
            cleaningImageCommandUseCase.deleteByChecklistId(checklistId);
        }
    }

    private boolean isRequiredImage(Checklist checklist) {
        return getCleaningForChecklistQuery.findById(checklist.getCleaningId())
                .map(GetCleaningForChecklistQuery.CleaningInfo::needPhoto)
                .orElse(false);
    }

    private void checkIsImageRegistered(Checklist checklist) {
        if (!cleaningImageQuery.isImagePresent(checklist.getChecklistId().value())) {
            throw new ChecklistRequireImageException(REQUIRE_IMAGE);
        }
    }

    @Override
    public void createChecklistByDateAndTime(Long cleaningId, List<CleaningDate> cleaningDates, Long placeId) {
        checklistCommandPort.createChecklistByDateAndTime(cleaningId, cleaningDates, placeId);
    }

    // ChecklistForCalendarUseCase 구현
    @Override
    public CompleteResult completeChecklist(Long checklistId, Long memberId) {
        Checklist completed = checklistCommandPort.completeChecklist(checklistId, memberId);
        return new CompleteResult(LocalTime.from(completed.getUpdatedAt()));
    }

    @Override
    public void deleteChecklist(Long checklistId) {
        cleaningImageCommandUseCase.deleteS3File(checklistId);
        checklistCommandPort.deleteById(checklistId);
    }
}
