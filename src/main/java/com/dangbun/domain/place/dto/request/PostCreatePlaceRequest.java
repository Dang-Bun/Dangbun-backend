package com.dangbun.domain.place.dto.request;

import java.util.Map;

public record PostCreatePlaceRequest (
        String placeName,
        String category,
        String memberName,
        Map<String, String > information
        ){}
