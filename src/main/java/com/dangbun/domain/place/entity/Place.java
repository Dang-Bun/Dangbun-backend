package com.dangbun.domain.place.entity;


import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.entity.DutyIcon;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    @Column(nullable = false, length = 50)
    @NotEmpty
    private String name;

    @Column(name = "category", nullable = false, length = 100)
    @NotEmpty
    private PlaceCategory category;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Duty> duties;

    private String code;

    @Builder
    public Place(String name, PlaceCategory category) {
        this.name = name;
        this.category = category;
    }
}
