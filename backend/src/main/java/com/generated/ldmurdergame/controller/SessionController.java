package com.generated.ldmurdergame.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.generated.ldmurdergame.dto.CancelResponse;
import com.generated.ldmurdergame.dto.PlayerActionRequest;
import com.generated.ldmurdergame.dto.RegisterResponse;
import com.generated.ldmurdergame.dto.SessionView;
import com.generated.ldmurdergame.service.SessionService;

@RestController
public class SessionController {
  private final SessionService sessionService;

  public SessionController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @GetMapping({"/sessions", "/api/sessions"})
  public List<SessionView> listSessions() {
    return sessionService.listSessions();
  }

  @PostMapping({"/sessions/{sessionId}/register", "/api/sessions/{sessionId}/register"})
  public RegisterResponse register(@PathVariable Long sessionId, @Valid @RequestBody PlayerActionRequest request) {
    return sessionService.register(sessionId, request.playerName());
  }

  @PostMapping({"/sessions/{sessionId}/cancel", "/api/sessions/{sessionId}/cancel"})
  public CancelResponse cancel(@PathVariable Long sessionId, @Valid @RequestBody PlayerActionRequest request) {
    return sessionService.cancel(sessionId, request.playerName());
  }
}
