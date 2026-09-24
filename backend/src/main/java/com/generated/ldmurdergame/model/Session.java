package com.generated.ldmurdergame.model;

public class Session {
  private Long id;
  private String title;
  private String scriptName;
  private String dmName;
  private String startTime;
  private int capacity;
  private String lastEvent;

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

  public String getScriptName() {
    return scriptName;
  }

  public void setScriptName(String scriptName) {
    this.scriptName = scriptName;
  }

  public String getDmName() {
    return dmName;
  }

  public void setDmName(String dmName) {
    this.dmName = dmName;
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  public String getLastEvent() {
    return lastEvent;
  }

  public void setLastEvent(String lastEvent) {
    this.lastEvent = lastEvent;
  }
}
