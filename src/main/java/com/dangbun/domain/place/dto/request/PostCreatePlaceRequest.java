package com.dangbun.domain.place.dto.request;

import com.dangbun.domain.place.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record PostCreatePlaceRequest(
        @Schema(description = "플레이스 이름", example = "메가박스")
        @NotBlank
        String placeName,

        @Schema(description = "카테고리(CAFE, RESTAURANT, THEATER, DORMITORY, BUILDING, OFFICE, SCHOOL, GYM, ETC)", example = "CAFE")
        @NotBlank
        PlaceCategory category,

        @Schema(description = "자유입력 세부 카테고리 (ETC일 때 필수)", example = "")
        String categoryName,

        @Schema(description = "매니저 이름", example = "홍길동")
        @NotBlank
        String managerName,

        @Schema(description = "정보", example = "{\"이메일\": \"test@test.com\" , \"전화번호\": \"01012345678\"}")
        Map<String, String> information
) {
    @AssertTrue(message = "ETC일 때는 categoryName이 반드시 필요합니다. ETC가 아니면 categoryName을 비워주세요.")
    public boolean isCategoryValid() {
        if (category == null) return false;
        boolean hasName = categoryName != null && !categoryName.trim().isEmpty();
        if (category == PlaceCategory.ETC) {
            return hasName;
        } else {
            return !hasName;
        }
    }
}
