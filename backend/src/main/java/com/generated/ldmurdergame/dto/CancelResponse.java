package com.generated.ldmurdergame.dto;

public record CancelResponse(
    boolean success,
    String message,
    String promotedPlayer,
    SessionView session) {
}
