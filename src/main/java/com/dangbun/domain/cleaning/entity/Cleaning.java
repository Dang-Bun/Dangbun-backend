package com.dangbun.domain.cleaning.entity;
import com.dangbun.domain.duty.entity.Duty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cleaning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cleaning_id")
    private Long cleaningId;

    @Column(nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private CleaningRepeatType RepeatType;

    @Column(name = "repeat_days", length = 20)
    private String repeatDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duty_id")
    private Duty duty;

    @Column(name = "need_photo", nullable = false)
    private Boolean needPhoto;

}
