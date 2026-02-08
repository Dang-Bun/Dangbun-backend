package com.dangbun.domain.member.adapter.in.web.dto.request;

import com.dangbun.domain.member.application.port.in.command.ExitPlaceCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ExitPlaceRequest(
        @Schema(description = "플레이스 이름", example = "메가박스")
        @NotBlank
        String placeName
) {
    public ExitPlaceCommand toCommand() {
        return new ExitPlaceCommand(placeName);
    }
}
