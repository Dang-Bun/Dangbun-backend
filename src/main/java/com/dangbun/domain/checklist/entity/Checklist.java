package com.dangbun.domain.checklist.entity;

import com.dangbun.domain.checklist.exception.custom.ChecklistStatusConflictException;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.global.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.dangbun.domain.checklist.response.status.ChecklistExceptionResponse.*;

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
    private Long completeMemberId;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Builder
    public Checklist(Long checklistId, Cleaning cleaning, Boolean isComplete, Long completeMemberId, LocalDateTime completeTime) {
        this.checklistId = checklistId;
        this.cleaning = cleaning;
        this.isComplete = isComplete;
        this.completeMemberId = completeMemberId;
        this.completeTime = completeTime;
    }


    public void completeChecklist(Member member){
        if(this.isComplete == true){
            throw new ChecklistStatusConflictException(ALREADY_CHECKED);
        }
        this.isComplete =true;
        this.completeMemberId = member.getMemberId();
        this.completeTime = LocalDateTime.now();
    }

    public void incompleteChecklist(){
        if(this.isComplete == false){
            throw new ChecklistStatusConflictException(ALREADY_UNCHECKED);
        }
        this.isComplete = false;
        this.completeMemberId = null;
        this.completeTime = null;
    }
}
