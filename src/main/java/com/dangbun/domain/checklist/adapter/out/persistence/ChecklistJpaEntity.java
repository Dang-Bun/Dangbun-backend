package com.dangbun.domain.checklist.adapter.out.persistence;

import com.dangbun.domain.checklist.exception.custom.ChecklistStatusConflictException;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

import static com.dangbun.domain.checklist.response.status.ChecklistExceptionResponse.ALREADY_CHECKED;
import static com.dangbun.domain.checklist.response.status.ChecklistExceptionResponse.ALREADY_UNCHECKED;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id")
    private Long checklistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaning_id")
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CleaningJpaEntity cleaningJpaEntity;

    @Column(name = "is_complete")
    private Boolean isComplete;

    @Column(name = "complete_member_id")
    private Long completeMemberId;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Builder
    public ChecklistJpaEntity(Long checklistId, CleaningJpaEntity cleaningJpaEntity, Boolean isComplete, Long completeMemberId, LocalDateTime completeTime) {
        this.checklistId = checklistId;
        this.cleaningJpaEntity = cleaningJpaEntity;
        this.isComplete = isComplete;
        this.completeMemberId = completeMemberId;
        this.completeTime = completeTime;
    }


    public void completeChecklist(Long memberId){
        if(this.isComplete == true){
            throw new ChecklistStatusConflictException(ALREADY_CHECKED);
        }
        this.isComplete =true;
        this.completeMemberId = memberId;
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
