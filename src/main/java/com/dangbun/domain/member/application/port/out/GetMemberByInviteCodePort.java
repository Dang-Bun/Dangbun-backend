package com.dangbun.domain.member.application.port.out;

import com.dangbun.domain.member.domain.Member;

public interface GetMemberByInviteCodePort {
    Member getMember(String inviteCode);
}
