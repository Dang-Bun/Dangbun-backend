package com.dangbun.domain.cleaningImage.adapter.out.persistence;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "cleaning_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CleaningImageJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cleaning_image_id")
    private Long cleaningImageId;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "uploader")
    private String uploader;

    /*
     * TODO: Checklist 도메인 헥사고날 아키텍처 전환 시 수정
     * Checklist -> ChecklistJpaEntity
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Checklist checklist;

    @Builder
    public CleaningImageJpaEntity(Long cleaningImageId, String s3Key, String uploader, Checklist checklist) {
        this.cleaningImageId = cleaningImageId;
        this.s3Key = s3Key;
        this.uploader = uploader;
        this.checklist = checklist;
    }
}
