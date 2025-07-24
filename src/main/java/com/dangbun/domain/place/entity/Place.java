package com.dangbun.domain.place.entity;


import com.dangbun.domain.duty.entity.Duty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 100)
    @NotNull
    private PlaceCategory category;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Duty> duties;

    @Column(name = "invite_code")
    private String inviteCode;

    @Builder
    public Place(String name, PlaceCategory category) {
        this.name = name;
        this.category = category;
    }

    public String createCode(String code){
        if(this.inviteCode != null)
            return this.inviteCode;

        this.inviteCode = code;
        return this.inviteCode;
    }
}
