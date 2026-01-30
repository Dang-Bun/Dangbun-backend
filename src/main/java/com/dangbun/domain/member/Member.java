package com.dangbun.domain.member;

import com.dangbun.domain.member.entity.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
public class Member {

    @Getter
    private Long memberId;

    @Getter
    private MemberRole role;

    @Getter
    private String name;

    @Getter
    private Boolean status;

    @Getter
    private Map<String, String> information;

    @Getter
    private Long placeId;

    @Getter
    private Long userId;

    @Builder
    public static Member withoutId(MemberRole role, String name, Boolean status, Map information, Long placeId, Long userId) {
        return new Member(null, role, name, status, information, placeId, userId);
    }

    @Builder(builderMethodName = "withIdBuilder")
    public static Member withId(Long memberId, MemberRole role, String name, Boolean status, Map information, Long placeId, Long userId) {
        return new Member(memberId, role, name, status, information, placeId, userId);
    }

    public void activate() {
        this.role = MemberRole.MEMBER;
        this.status = true;
    }
}
