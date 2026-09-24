package com.generated.ldmurdergame.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlayerActionRequest(
    @NotBlank(message = "玩家昵称不能为空") @Size(max = 80, message = "玩家昵称不能超过80个字符") String playerName) {
}
