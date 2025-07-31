package com.dangbun.domain.member;

import com.dangbun.domain.member.entity.Member;

public class MemberContext {
    private static final ThreadLocal<Member> currentMember = new ThreadLocal<>();

    public static void set(Member member){
        currentMember.set(member);
    }

    public static Member get(){
        return currentMember.get();
    }

    public static void clear(){
        currentMember.remove();
    }

}
