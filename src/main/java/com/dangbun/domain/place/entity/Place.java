package com.dangbun.domain.place.entity;


import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    @Column(nullable = false, length = 50)
    @NotEmpty
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 100)
    @NotNull
    private PlaceCategory category;

    @Column(name="category_name", length = 10)
    private String categoryName;

    @Column(name = "invite_code")
    private String inviteCode;

    @Column(name = "start_time")
    private LocalTime startTime = LocalTime.MIDNIGHT;

    @Column(name = "end_time")
    private LocalTime endTime = LocalTime.MAX;

    @Column(name = "is_today")
    private Boolean isToday = true;

    @Builder
    public Place(String name, PlaceCategory category, String categoryName) {
        this.name = name;
        this.category = category;
        this.categoryName = categoryName;
    }

    public String createCode(String code){
        if(this.inviteCode != null)
            return this.inviteCode;

        this.inviteCode = code;
        return this.inviteCode;
    }

    public void setTime(LocalTime startTime, LocalTime endTime, Boolean isToday){
        this.startTime = startTime;
        this.endTime = endTime;
        this.isToday = isToday;
    }
}
