package com.dangbun.domain.member.refactor.application.port.out;

import com.dangbun.domain.member.refactor.domain.Member;

public interface GetMemberByInviteCodePort {
    Member getMember(String inviteCode);
}
