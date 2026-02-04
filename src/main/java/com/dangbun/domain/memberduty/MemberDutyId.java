package com.dangbun.domain.memberduty;

import jakarta.persistence.Column;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

public class MemberDutyId implements Serializable {

    @Getter
    private Long memberId;

    @Getter
    private Long dutyId;

    public MemberDutyId(Long memberId, Long dutyId) {
        this.memberId = memberId;
        this.dutyId = dutyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof com.dangbun.domain.memberduty.MemberDutyId that)) return false;
        return Objects.equals(dutyId, that.dutyId) && Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dutyId, memberId);
    }
}
