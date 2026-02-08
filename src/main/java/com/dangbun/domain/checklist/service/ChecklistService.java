package com.dangbun.domain.checklist.service;

import com.dangbun.domain.checklist.refactor.adapter.in.web.dto.response.GetImageUrlResponse;
import com.dangbun.domain.checklist.refactor.adapter.in.web.dto.response.PostCompleteChecklistResponse;
import com.dangbun.domain.checklist.refactor.adapter.in.web.dto.response.PostGetPresignedUrlResponse;
import com.dangbun.domain.checklist.refactor.adapter.in.web.dto.response.PostIncompleteChecklistResponse;
import com.dangbun.domain.checklist.refactor.adapter.out.persistence.ChecklistJpaEntity;
import com.dangbun.domain.checklist.exception.custom.ChecklistRequireImageException;
import com.dangbun.domain.cleaningImage.application.port.in.command.CleaningImageCommandUseCase;
import com.dangbun.domain.cleaningImage.application.port.in.query.CleaningImageQuery;
import com.dangbun.global.context.ChecklistContext;
import com.dangbun.domain.checklist.refactor.adapter.in.web.dto.request.PostGetPresignedUrlRequest;
import com.dangbun.domain.checklist.refactor.adapter.in.web.dto.request.PostSaveUploadResultRequest;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.global.context.MemberContext;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.dangbun.domain.checklist.refactor.response.status.ChecklistExceptionResponse.REQUIRE_IMAGE;

@Transactional
@RequiredArgsConstructor
@Service
public class ChecklistService {

    private final CleaningImageCommandUseCase cleaningImageCommandUseCase;
    private final CleaningImageQuery cleaningImageQuery;

    private final MemberCleaningRepository memberCleaningRepository;
    private final ChecklistGenerateService checklistGenerateService;

    public PostCompleteChecklistResponse completeChecklist() {
        MemberJpaEntity member = MemberContext.get();
        ChecklistJpaEntity checklistJpaEntity = ChecklistContext.get();
        if(isRequiredImage(checklistJpaEntity)){
            checkIsImageRegistered(checklistJpaEntity);
        }
        checklistJpaEntity.completeChecklist(member);
        LocalDateTime endTime = checklistJpaEntity.getUpdatedAt();
        return PostCompleteChecklistResponse.of(member.getName(), LocalTime.from(endTime));
    }

    public PostIncompleteChecklistResponse incompleteChecklist() {
        ChecklistJpaEntity checklistJpaEntity = ChecklistContext.get();

        CleaningJpaEntity cleaningJpaEntity = checklistJpaEntity.getCleaningJpaEntity();
        List<MemberJpaEntity> members = memberCleaningRepository.findMembersByCleaningId(cleaningJpaEntity.getCleaningId());
        List<String> membersName = members.stream().map(MemberJpaEntity::getName).toList();
        LocalTime endTime = cleaningJpaEntity.getPlace().getEndTime();

        checklistJpaEntity.incompleteChecklist();

        return PostIncompleteChecklistResponse.of(checklistJpaEntity.getChecklistId(), membersName, endTime);

    }

    public PostGetPresignedUrlResponse generateImageUrl(PostGetPresignedUrlRequest request) {
        ChecklistJpaEntity checklistJpaEntity = ChecklistContext.get();
        Map<String, String> uploadUrlAndKey = cleaningImageCommandUseCase
                .generateUploadUrl(request.originalFileName(), request.contentType(), checklistJpaEntity.getChecklistId());


        return new PostGetPresignedUrlResponse(uploadUrlAndKey.get("uploadUrl"), uploadUrlAndKey.get("s3Key"));
    }

    public void saveUploadResult(PostSaveUploadResultRequest request) {
        ChecklistJpaEntity checklistJpaEntity = ChecklistContext.get();
        cleaningImageCommandUseCase.saveImage(checklistJpaEntity.getChecklistId(), request.s3Key());
    }

    public GetImageUrlResponse getImageUrl(Long checklistId) {
        String accessUrl = cleaningImageQuery.getImageUrl(checklistId);
        return new GetImageUrlResponse(accessUrl);
    }

    public void deleteS3Key(Long checklistId) {
        if(cleaningImageQuery.isImagePresent(checklistId)){
            cleaningImageCommandUseCase.deleteByChecklistId(checklistId);
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void scheduledChecklistGeneration() {
        checklistGenerateService.generateDailyChecklists(LocalDateTime.now());
    }


    private boolean isRequiredImage(ChecklistJpaEntity checklistJpaEntity) {
        CleaningJpaEntity cleaningJpaEntity = checklistJpaEntity.getCleaningJpaEntity();
        return cleaningJpaEntity.getNeedPhoto();
    }

    private void checkIsImageRegistered(ChecklistJpaEntity checklistJpaEntity){
        if(!cleaningImageQuery.isImagePresent(checklistJpaEntity.getChecklistId())){
            throw new ChecklistRequireImageException(REQUIRE_IMAGE);
        }
    }
}