package com.dangbun.domain.place.entity;


import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.entity.DutyIcon;
import jakarta.persistence.*;
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
    private String name;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Duty> duties;

    @Builder
    public Place(String name, String category) {
        this.name = name;
        this.category = category;
    }
}
