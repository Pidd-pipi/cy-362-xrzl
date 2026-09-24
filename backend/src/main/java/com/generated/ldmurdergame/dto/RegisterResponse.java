package com.generated.ldmurdergame.dto;

public record RegisterResponse(
    boolean success,
    String status,
    String message,
    int aheadCount,
    SessionView session) {
}
