package com.dangbun.domain.member.controller;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/places/{placeId}/members")
@RestController
public class MemberController {

    @Operation(summary = "맴버 목록 조회",description = "플레이스에 참가한 맴버를 조회합니다." )
    @GetMapping
    public ResponseEntity<?> getMembers(@PathVariable("placeId") Long placeId){
        return null;
    }

    @Operation(summary = "대기 맴버 목록 조회",description = "현재 플레이스 참가를 대기하고 있는 맴버들을 조회합니다.(매니저용)")
    @GetMapping("/waiting")
    public ResponseEntity<?> getWaitingMembers(@PathVariable("placeId") Long placeId){
        return null;
    }

    @Operation(summary = "맴버 수락",description = "대기중인 맴버의 참가를 수락합니다.(매니저용)")
    @PostMapping("/accept")
    public ResponseEntity<?> registerMember(@PathVariable("placeId") Long placeId){
        return null;
    }

    @Operation(summary = "맴버 거절",description = "대기중인 맴버의 참가를 거절합니다.(매니저용)")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable("placeId") Long placeId,
                                          @PathVariable("memberId") Long memberId){

        return null;
    }

    @Operation(summary = "맴버 정보 조회", description = "한 맴버에 대한 정보를 조회합니다.")
    @GetMapping("/{memberId}")
    public ResponseEntity<?> getMember(@PathVariable("placeId") Long placeId,
                                       @PathVariable("memberId") Long memberId){

        return null;
    }

    @Operation(summary = "플레이스 나가기", description = "플레이스에서 나갑니다")
    @DeleteMapping("/me")
    public ResponseEntity<?> removeSelfFromPlace(@AuthenticationPrincipal(expression = "user") User user,
                                      @PathVariable("placeId") Long placeId){

        return null;
    }

}
