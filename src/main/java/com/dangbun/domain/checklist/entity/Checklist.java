package com.dangbun.domain.checklist.entity;

import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.global.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Checklist extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id")
    private Long checklistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaning_id")
    @NotNull
    private Cleaning cleaning;

    @Column(name = "is_complete")
    private Boolean isComplete;

    @Column(name = "complete_member_id")
    private String completeMemberId;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    public void completeChecklist(){
        this.isComplete =true;
    }

    public void unCompleteChecklist(){
        this.isComplete = false;
    }
}
