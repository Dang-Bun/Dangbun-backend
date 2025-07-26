package com.dangbun.domain.cleaningdate.entity;

import com.dangbun.domain.cleaning.entity.Cleaning;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="cleaning_date")
public class CleaningDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="cleaning_date_id")
    private Long cleaningDateId;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaning_id", nullable = false)
    private Cleaning cleaning;

    @Builder
    public CleaningDate(LocalDate date, Cleaning cleaning) {
        this.date = date;
        this.cleaning = cleaning;
    }

}
