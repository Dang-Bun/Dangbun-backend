package com.dangbun.domain.membercleaning.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
public class MemberCleaningId implements Serializable {

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "cleaning_id")
    private Long cleaningId;

    public MemberCleaningId(Long memberId, Long cleaningId) {
        this.memberId = memberId;
        this.cleaningId = cleaningId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof com.dangbun.domain.membercleaning.entity.MemberCleaningId that)) return false;
        return Objects.equals(cleaningId, that.cleaningId) && Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cleaningId, memberId);
    }
}