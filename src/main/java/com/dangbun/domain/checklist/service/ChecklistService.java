package com.dangbun.domain.checklist.service;

import com.dangbun.domain.checklist.dto.response.*;
import com.dangbun.domain.checklist.exception.custom.ChecklistRequireImageException;
import com.dangbun.domain.cleaningImage.application.port.in.command.CleaningImageCommandUseCase;
import com.dangbun.domain.cleaningImage.application.port.in.query.CleaningImageQuery;
import com.dangbun.global.context.ChecklistContext;
import com.dangbun.domain.checklist.dto.request.PostGetPresignedUrlRequest;
import com.dangbun.domain.checklist.dto.request.PostSaveUploadResultRequest;
import com.dangbun.domain.checklist.entity.Checklist;
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

import static com.dangbun.domain.checklist.response.status.ChecklistExceptionResponse.*;

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
        Checklist checklist = ChecklistContext.get();
        if(isRequiredImage(checklist)){
            checkIsImageRegistered(checklist);
        }
        checklist.completeChecklist(member);
        LocalDateTime endTime = checklist.getUpdatedAt();
        return PostCompleteChecklistResponse.of(member.getName(), LocalTime.from(endTime));
    }

    public PostIncompleteChecklistResponse incompleteChecklist() {
        Checklist checklist = ChecklistContext.get();

        CleaningJpaEntity cleaningJpaEntity = checklist.getCleaningJpaEntity();
        List<MemberJpaEntity> members = memberCleaningRepository.findMembersByCleaningId(cleaningJpaEntity.getCleaningId());
        List<String> membersName = members.stream().map(MemberJpaEntity::getName).toList();
        LocalTime endTime = cleaningJpaEntity.getPlace().getEndTime();

        checklist.incompleteChecklist();

        return PostIncompleteChecklistResponse.of(checklist.getChecklistId(), membersName, endTime);

    }

    public PostGetPresignedUrlResponse generateImageUrl(PostGetPresignedUrlRequest request) {
        Checklist checklist = ChecklistContext.get();
        Map<String, String> uploadUrlAndKey = cleaningImageCommandUseCase
                .generateUploadUrl(request.originalFileName(), request.contentType(), checklist.getChecklistId());


        return new PostGetPresignedUrlResponse(uploadUrlAndKey.get("uploadUrl"), uploadUrlAndKey.get("s3Key"));
    }

    public void saveUploadResult(PostSaveUploadResultRequest request) {
        Checklist checklist = ChecklistContext.get();
        cleaningImageCommandUseCase.saveImage(checklist.getChecklistId(), request.s3Key());
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


    private boolean isRequiredImage(Checklist checklist) {
        CleaningJpaEntity cleaningJpaEntity = checklist.getCleaningJpaEntity();
        return cleaningJpaEntity.getNeedPhoto();
    }

    private void checkIsImageRegistered(Checklist checklist){
        if(!cleaningImageQuery.isImagePresent(checklist.getChecklistId())){
            throw new ChecklistRequireImageException(REQUIRE_IMAGE);
        }
    }
}