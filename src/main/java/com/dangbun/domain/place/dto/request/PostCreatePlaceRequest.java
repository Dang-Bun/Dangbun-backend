package com.dangbun.domain.place.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record PostCreatePlaceRequest (
        @NotEmpty String placeName,
        @NotEmpty String category,
        @NotEmpty String memberName,
        Map<String, String > memberInformation
        ){}
