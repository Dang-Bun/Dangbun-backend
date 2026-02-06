package com.dangbun.domain.memberduty.adapter.out.persistence;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
public class MemberDutyJpaEntityId implements Serializable {

    private Long memberId;

    private Long dutyId;

    public MemberDutyJpaEntityId(Long memberId, Long dutyId) {
        this.memberId = memberId;
        this.dutyId = dutyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberDutyJpaEntityId that)) return false;
        return Objects.equals(dutyId, that.dutyId) && Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dutyId, memberId);
    }
}
