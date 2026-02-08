package com.dangbun.domain.cleaningImage.application.port.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.cleaningImage.application.port.in.query.CleaningImageQuery;
import com.dangbun.domain.cleaningImage.application.port.out.CleaningImageQueryPort;
import com.dangbun.domain.cleaningImage.domain.CleaningImage;
import com.dangbun.domain.cleaningImage.exception.custom.NoSuchImageException;
import com.dangbun.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.dangbun.domain.cleaningImage.exception.status.CleaningImageExceptionResponse.NO_SUCH_IMAGE;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CleaningImageQueryService implements CleaningImageQuery {

    private final CleaningImageQueryPort cleaningImageQueryPort;
    private final S3Service s3Service;

    @Override
    public String getImageUrl(Long checklistId) {
        CleaningImage cleaningImage = cleaningImageQueryPort.findByChecklistId(checklistId)
                .orElseThrow(() -> new NoSuchImageException(NO_SUCH_IMAGE));

        return s3Service.generateDownloadUrl(cleaningImage.getS3Key());
    }

    @Override
    public boolean isImagePresent(Long checklistId) {
        return cleaningImageQueryPort.existsByChecklistId(checklistId);
    }
}
