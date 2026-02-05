package com.dangbun.domain.membercleaning.refactor.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MemberCleaning {

    private final MemberCleaningId memberCleaningId;

    private final Long memberId;

    private final Long cleaningId;

    public static MemberCleaning of(Long memberId, Long cleaningId) {
        return new MemberCleaning(
                new MemberCleaningId(memberId, cleaningId),
                memberId,
                cleaningId
        );
    }

    public record MemberCleaningId(Long memberId, Long cleaningId) {
    }
}
