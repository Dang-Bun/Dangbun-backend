package com.dangbun.domain.place.application.port.in.command;

import java.util.Map;

public record JoinPlaceCommand(
        Long userId,
        String inviteCode,
        String name,
        Map<String, String> information
) {
    public static JoinPlaceCommand of(Long userId, String inviteCode, String name, Map<String, String> information){
        return new JoinPlaceCommand(userId, inviteCode, name, information);
    }
}
