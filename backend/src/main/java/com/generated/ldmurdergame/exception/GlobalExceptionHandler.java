package com.generated.ldmurdergame.exception;

import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, String>> handleApiException(ApiException exception) {
    return ResponseEntity.status(exception.getStatus())
      .body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream()
      .findFirst()
      .map(error -> error.getDefaultMessage())
      .orElse("请求参数不合法");
    return ResponseEntity.badRequest().body(Map.of("message", message));
  }

  /** 唯一约束兜底：多实例并发时同一玩家重复提交也只会保留一条报名。 */
  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<Map<String, String>> handleDuplicateKey(DuplicateKeyException exception) {
    return ResponseEntity.badRequest()
      .body(Map.of("message", "该玩家已报名此场次，请勿重复提交。"));
  }
}
