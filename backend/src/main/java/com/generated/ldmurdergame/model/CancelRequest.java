package com.generated.ldmurdergame.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelRequest(
  @NotBlank(message = "玩家昵称不能为空")
  @Size(max = 80, message = "玩家昵称最长 80 个字符")
  String playerName
) {
}
