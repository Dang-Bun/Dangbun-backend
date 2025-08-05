package com.dangbun.global.s3;

import com.dangbun.domain.cleaningImage.exception.custom.InvalidS3KeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static com.dangbun.domain.cleaningImage.response.status.CleaningImageExceptionResponse.*;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public Map<String, String> generateUploadUrl(String fileName, String contentType, Long userId, Long checklistId) {
        String key = "uploads/user-" + userId + "/" + UUID.randomUUID() + "-" + fileName;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        redisTemplate.opsForValue().set("S3 Key" + checklistId, key, Duration.ofMinutes(6));

        PresignedPutObjectRequest presignedUrl = s3Presigner.presignPutObject(b -> b
                .putObjectRequest(request)
                .signatureDuration(Duration.ofMinutes(5))
        );


        return Map.of(
                "uploadUrl", presignedUrl.url().toString(),
                "s3Key", key);
    }

    public String generateDownloadUrl(String s3Key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(b -> b
                .getObjectRequest(request)
                .signatureDuration(Duration.ofMinutes(5))
        );

        return presignedRequest.url().toString();
    }

    public void validateKey(Long checklistId, String s3Key) {
        String savedKey = (String)redisTemplate.opsForValue().get("S3 Key" + checklistId);
        if(savedKey == null || !savedKey.equals(s3Key)){
            throw new InvalidS3KeyException(INVALID_S3_KEY);
        }
    }
}