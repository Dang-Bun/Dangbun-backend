package com.dangbun.domain.cleaningdate.entity;

import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="cleaning_date")
public class CleaningDateJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="cleaning_date_id")
    private Long cleaningDateId;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaning_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CleaningJpaEntity cleaningJpaEntity;

    @Builder
    public CleaningDateJpaEntity(LocalDate date, CleaningJpaEntity cleaningJpaEntity) {
        this.date = date;
        this.cleaningJpaEntity = cleaningJpaEntity;
    }

}
