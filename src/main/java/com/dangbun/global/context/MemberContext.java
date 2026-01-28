package com.dangbun.global.context;

import com.dangbun.domain.member.entity.MemberJpaEntity;

public class MemberContext {
    private static final ThreadLocal<MemberJpaEntity> currentMember = new ThreadLocal<>();

    public static void set(MemberJpaEntity member){
        currentMember.set(member);
    }

    public static MemberJpaEntity get(){
        return currentMember.get();
    }

    public static void clear(){
        currentMember.remove();
    }

}
