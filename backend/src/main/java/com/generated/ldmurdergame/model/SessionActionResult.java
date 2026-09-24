package com.generated.ldmurdergame.model;

/**
 * 报名 / 退局操作结果：提示语 + 操作后的场次最新状态。
 */
public record SessionActionResult(
  String message,
  String status,
  Integer waitingPosition,
  SessionView session
) {
}
