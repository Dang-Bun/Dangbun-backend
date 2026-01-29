package com.dangbun.domain.place.refactor.application.port.in.command;

import com.dangbun.domain.place.refactor.domain.PlaceCategory;
import com.dangbun.domain.place.refactor.SelfValidating;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

import java.util.Map;

@Value
public class CreatePlaceCommand extends SelfValidating<CreatePlaceCommand> {

    @NotBlank
    Long userId;

    @NotBlank
    String placeName;

    @NotBlank
    PlaceCategory category;

    String categoryName;

    @NotBlank
    String managerName;

    Map<String, String> information;

    public CreatePlaceCommand(
            Long userId,
            String placeName,
            PlaceCategory category,
            String categoryName,
            String managerName,
            Map<String, String> information
    ) {
        this.userId = userId;
        this.placeName = placeName;
        this. category = category;
        this.categoryName = categoryName;
        this.managerName = managerName;
        this.information = information;
        this.validateSelf();
    }

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
