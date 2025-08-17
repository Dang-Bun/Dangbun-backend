package com.dangbun.domain.cleaning.entity;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.place.entity.Place;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cleaning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cleaning_id")
    private Long cleaningId;

    @Column(nullable = false, length = 20, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private CleaningRepeatType repeatType;

    @Column(name = "repeat_days", length = 20)
    private String repeatDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duty_id")
    private Duty duty;

    @Column(name = "need_photo", nullable = false)
    private Boolean needPhoto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Place place;

    @OneToMany(mappedBy = "cleaning")
    private List<MemberCleaning> memberCleanings = new ArrayList<>();


    @Builder
    public Cleaning(String name, CleaningRepeatType repeatType, String repeatDays, Duty duty, Boolean needPhoto, Place place) {
        this.name = name;
        this.repeatType = repeatType;
        this.repeatDays = repeatDays;
        this.duty = duty;
        this.needPhoto = needPhoto;
        this.place = place;
    }

    public void updateInfo(String name, Boolean needPhoto, CleaningRepeatType repeatType, String repeatDays, Duty duty) {
        this.name = name;
        this.needPhoto = needPhoto;
        this.repeatType = repeatType;
        this.repeatDays = repeatDays;
        this.duty = duty;
    }

    public void updateMembers(List<Member> newMembers) {
        this.memberCleanings.removeIf(mc -> true);
        for (Member member : newMembers) {
            MemberCleaning mc = MemberCleaning.builder()
                    .cleaning(this)
                    .member(member)
                    .build();
            this.memberCleanings.add(mc);
        }
    }

    public void assignToDuty(Duty duty) {
        this.duty = duty;
    }

    public void removeDuty() {
        this.duty = null;
    }
}
