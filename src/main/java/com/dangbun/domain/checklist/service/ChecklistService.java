package com.dangbun.domain.checklist.service;

import com.dangbun.domain.checklist.ChecklistContext;
import com.dangbun.domain.checklist.dto.request.PostGetPresignedUrlRequest;
import com.dangbun.domain.checklist.dto.request.PostSaveUploadResultRequest;
import com.dangbun.domain.checklist.dto.response.GetImageUrlResponse;
import com.dangbun.domain.checklist.dto.response.PostCompleteChecklistResponse;
import com.dangbun.domain.checklist.dto.response.PostGetPresignedUrlResponse;
import com.dangbun.domain.checklist.dto.response.PostIncompleteChecklistResponse;
import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaningImage.service.CleaningImageService;
import com.dangbun.global.context.MemberContext;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Transactional
@RequiredArgsConstructor
@Service
public class ChecklistService {


    private final ChecklistRepository checklistRepository;
    private final CleaningImageService cleaningImageService;
    private final MemberCleaningRepository memberCleaningRepository;

    public void deleteChecklist(Long checklistId) {
        checklistRepository.deleteById(checklistId);
    }

    public PostCompleteChecklistResponse completeChecklist() {
        Member member = MemberContext.get();
        Checklist checklist = ChecklistContext.get();
        checklist.completeChecklist();
        LocalDateTime endTime = checklist.getUpdatedAt();
        return PostCompleteChecklistResponse.of(member.getName(), LocalTime.from(endTime));
    }

    public PostIncompleteChecklistResponse incompleteChecklist() {
        Checklist checklist = ChecklistContext.get();

        Cleaning cleaning = checklist.getCleaning();
        List<Member> members = memberCleaningRepository.findMembersByCleaningId(cleaning.getCleaningId());
        List<String> membersName = members.stream().map(Member::getName).toList();
        LocalTime endTime = cleaning.getPlace().getEndTime();

        checklist.incompleteChecklist();

        return PostIncompleteChecklistResponse.of(checklist.getChecklistId(), membersName, endTime);

    }

    public PostGetPresignedUrlResponse generateImageUrl(PostGetPresignedUrlRequest request) {
        Checklist checklist = ChecklistContext.get();
        Map<String, String> uploadUrlAndKey = cleaningImageService
                .generateUrl(request.originalFileName(), request.contentType(),checklist.getChecklistId());


        return new PostGetPresignedUrlResponse(uploadUrlAndKey.get("uploadUrl"), uploadUrlAndKey.get("key"));
    }

    public void saveUploadResult(PostSaveUploadResultRequest request) {
        Checklist checklist = ChecklistContext.get();
        cleaningImageService.saveImage(checklist,request.s3Key());
    }

    public GetImageUrlResponse getImageUrl(Long checklistId) {
        String accessUrl = cleaningImageService.getImageUrl(checklistId);
        return new GetImageUrlResponse(accessUrl);
    }
}
