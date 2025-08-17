package com.dangbun.domain.cleaningImage.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.cleaningImage.entity.CleaningImage;
import com.dangbun.domain.cleaningImage.repository.CleaningImageRepository;
import com.dangbun.domain.user.entity.CustomUserDetails;
import com.dangbun.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class CleaningImageService {

    private final S3Service s3Service;
    private final CleaningImageRepository cleaningImageRepository;

    public Map<String, String> generateUrl(String filename, String contentType, Long checklistId) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CustomUserDetails userDetails = (CustomUserDetails) principal;
        Long uploaderId = userDetails.getUser().getUserId();

        return s3Service.generateUploadUrl(filename, contentType, uploaderId, checklistId);
    }


    public void saveImage(Checklist checklist, String s3Key) {

        s3Service.validateKey(checklist.getChecklistId(), s3Key);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CustomUserDetails userDetails = (CustomUserDetails) principal;
        Long userId = userDetails.getUser().getUserId();

        CleaningImage ci = CleaningImage.builder().checklist(checklist)
                .uploader(userId.toString())
                .s3Key(s3Key)
                .build();

        cleaningImageRepository.save(ci);
    }

    public String getImageUrl(Long checklistId) {
        CleaningImage cleaningImage = cleaningImageRepository.findByChecklist_ChecklistId(checklistId).get();
        String s3Key = cleaningImage.getS3Key();

        String accessUrl = s3Service.generateDownloadUrl(s3Key);

        return accessUrl;
    }
}
