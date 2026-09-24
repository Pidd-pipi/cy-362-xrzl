package com.generated.ldmurdergame.dto;

import java.util.List;

public record SessionView(
    Long id,
    String title,
    String scriptName,
    String dmName,
    String startTime,
    int capacity,
    int seatedCount,
    int remainingSeats,
    int waitingCount,
    String lastEvent,
    List<PlayerView> seatedPlayers,
    List<WaitingPlayerView> waitingPlayers) {
}
