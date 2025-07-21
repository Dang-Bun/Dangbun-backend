package com.dangbun.domain.memberduty.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
public class MemberDutyId implements Serializable {
    @Column(name = "duty_id")
    private Long dutyId;

    @Column(name = "member_id")
    private Long memberId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberDutyId that)) return false;
        return Objects.equals(dutyId, that.dutyId) && Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dutyId, memberId);
    }
}
