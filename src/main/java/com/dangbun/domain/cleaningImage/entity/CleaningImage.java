package com.dangbun.domain.cleaningImage.entity;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@NoArgsConstructor
public class CleaningImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cleaning_image_id")
    Long id;

    @Getter
    @Column(name = "s3_key")
    String s3Key;

    String uploader;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Checklist checklist;

    @Builder
    public CleaningImage(Long id, String s3Key, String uploader, Checklist checklist) {
        this.id = id;
        this.s3Key = s3Key;
        this.uploader = uploader;
        this.checklist = checklist;
    }


}
