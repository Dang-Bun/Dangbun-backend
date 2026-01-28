package com.dangbun.domain.place.refactor.domain;

import lombok.Getter;

import java.util.List;

@Getter
public class Information {
    private Long placeId;

    private List<String> information;

    public Information(Long placeId, List<String> information) {
        this.placeId = placeId;
        this.information = information;
    }
}
