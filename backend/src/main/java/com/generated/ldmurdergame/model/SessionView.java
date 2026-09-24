package com.generated.ldmurdergame.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 总览页场次视图：座位占用、候补队列与最近一次补位结果。
 */
public record SessionView(
  Long id,
  String title,
  LocalDateTime startTime,
  int capacity,
  int seatedCount,
  int freeSeats,
  boolean full,
  int waitingCount,
  String playerStatus,
  Integer waitingPosition,
  String lastPromotedPlayer,
  LocalDateTime lastPromotedAt,
  String lastPromotionNote,
  List<String> seatedPlayers,
  List<WaitingPlayer> waitingPlayers
) {
  public record WaitingPlayer(String playerName, int position) {
  }
}
