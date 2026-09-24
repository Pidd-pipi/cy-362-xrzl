package com.generated.ldmurdergame.model;

import java.time.LocalDateTime;

public class Session {
  private Long id;
  private String title;
  private LocalDateTime startTime;
  private Integer capacity;
  private String lastPromotedPlayer;
  private LocalDateTime lastPromotedAt;
  private String lastPromotionNote;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public Integer getCapacity() {
    return capacity;
  }

  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }

  public String getLastPromotedPlayer() {
    return lastPromotedPlayer;
  }

  public void setLastPromotedPlayer(String lastPromotedPlayer) {
    this.lastPromotedPlayer = lastPromotedPlayer;
  }

  public LocalDateTime getLastPromotedAt() {
    return lastPromotedAt;
  }

  public void setLastPromotedAt(LocalDateTime lastPromotedAt) {
    this.lastPromotedAt = lastPromotedAt;
  }

  public String getLastPromotionNote() {
    return lastPromotionNote;
  }

  public void setLastPromotionNote(String lastPromotionNote) {
    this.lastPromotionNote = lastPromotionNote;
  }
}
