package com.generated.ldmurdergame.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.generated.ldmurdergame.model.CancelRequest;
import com.generated.ldmurdergame.model.RegisterRequest;
import com.generated.ldmurdergame.model.SessionActionResult;
import com.generated.ldmurdergame.model.SessionView;
import com.generated.ldmurdergame.service.SessionBoardService;

/**
 * 总览页场次报名 / 退局接口，同时暴露裸路径与 /api 前缀（由 Nginx 去掉前缀转发）。
 */
@RestController
public class SessionController {
  private final SessionBoardService sessionBoardService;

  public SessionController(SessionBoardService sessionBoardService) {
    this.sessionBoardService = sessionBoardService;
  }

  @GetMapping({"/sessions", "/api/sessions"})
  public List<SessionView> sessions() {
    return sessionBoardService.listSessions();
  }

  @GetMapping({"/sessions/{id}", "/api/sessions/{id}"})
  public SessionView session(@PathVariable Long id) {
    return sessionBoardService.getSession(id);
  }

  @PostMapping({"/sessions/{id}/register", "/api/sessions/{id}/register"})
  public SessionActionResult register(@PathVariable Long id,
                                      @Valid @RequestBody RegisterRequest request) {
    return sessionBoardService.register(id, request.playerName());
  }

  @PostMapping({"/sessions/{id}/cancel", "/api/sessions/{id}/cancel"})
  public SessionActionResult cancel(@PathVariable Long id,
                                    @Valid @RequestBody CancelRequest request) {
    return sessionBoardService.cancel(id, request.playerName());
  }
}
