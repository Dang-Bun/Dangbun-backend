package com.dangbun.domain.member.application.port.in.query;

import com.dangbun.domain.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetMemberPageQuery {
    Page<Member> getPagedMemberByPlaceId(Long placeId, Pageable pageable);

    Page<Member> getPageMemberByPlaceIdAndNameContaining(Long placeId, String searchName, Pageable pageable);
}
