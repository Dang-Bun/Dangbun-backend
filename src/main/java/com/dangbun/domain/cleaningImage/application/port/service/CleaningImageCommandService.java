package com.dangbun.domain.cleaningImage.application.port.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.cleaningImage.application.port.in.command.CleaningImageCommandUseCase;
import com.dangbun.domain.cleaningImage.application.port.out.CleaningImageCommandPort;
import com.dangbun.domain.cleaningImage.application.port.out.CleaningImageQueryPort;
import com.dangbun.domain.cleaningImage.domain.CleaningImage;
import com.dangbun.domain.cleaningImage.exception.custom.CleaningImageAlreadyExistsException;
import com.dangbun.domain.user.detail.CustomUserDetails;
import com.dangbun.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.dangbun.domain.cleaningImage.exception.status.CleaningImageExceptionResponse.CLEANING_IMAGE_ALREADY_EXISTS;

@UseCase
@RequiredArgsConstructor
@Transactional
public class CleaningImageCommandService implements CleaningImageCommandUseCase {

    private final CleaningImageQueryPort cleaningImageQueryPort;
    private final CleaningImageCommandPort cleaningImageCommandPort;
    private final S3Service s3Service;

    @Override
    public Map<String, String> generateUploadUrl(String filename, String contentType, Long checklistId) {
        if (cleaningImageQueryPort.existsByChecklistId(checklistId)) {
            throw new CleaningImageAlreadyExistsException(CLEANING_IMAGE_ALREADY_EXISTS);
        }

        Long uploaderId = getCurrentUserId();

        return s3Service.generateUploadUrl(filename, contentType, uploaderId, checklistId);
    }

    @Override
    public void saveImage(Long checklistId, String s3Key) {
        s3Service.validateKey(checklistId, s3Key);

        Long userId = getCurrentUserId();

        CleaningImage cleaningImage = CleaningImage.withoutId(
                s3Key,
                userId.toString(),
                checklistId
        );

        cleaningImageCommandPort.save(cleaningImage);
    }

    @Override
    public void deleteByChecklistId(Long checklistId) {
        cleaningImageCommandPort.deleteByChecklistId(checklistId);
    }

    @Override
    public void deleteS3File(Long checklistId) {
        cleaningImageQueryPort.findByChecklistId(checklistId)
                .ifPresent(
                        img -> s3Service.deleteFile(img.getS3Key())
                );
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CustomUserDetails userDetails = (CustomUserDetails) principal;
        return userDetails.getUser().getUserId();
    }
}
