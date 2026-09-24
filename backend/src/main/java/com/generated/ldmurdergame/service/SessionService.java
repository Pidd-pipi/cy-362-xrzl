package com.generated.ldmurdergame.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.generated.ldmurdergame.dto.CancelResponse;
import com.generated.ldmurdergame.dto.PlayerView;
import com.generated.ldmurdergame.dto.RegisterResponse;
import com.generated.ldmurdergame.dto.SessionView;
import com.generated.ldmurdergame.dto.WaitingPlayerView;
import com.generated.ldmurdergame.exception.ApiException;
import com.generated.ldmurdergame.mapper.RegistrationMapper;
import com.generated.ldmurdergame.mapper.SessionMapper;
import com.generated.ldmurdergame.model.Registration;
import com.generated.ldmurdergame.model.Session;

@Service
public class SessionService {
  private final SessionMapper sessionMapper;
  private final RegistrationMapper registrationMapper;

  public SessionService(SessionMapper sessionMapper, RegistrationMapper registrationMapper) {
    this.sessionMapper = sessionMapper;
    this.registrationMapper = registrationMapper;
  }

  public List<SessionView> listSessions() {
    return sessionMapper.findAll().stream().map(this::toView).toList();
  }

  /**
   * 报名：有空位则占座，满员则进入候补。
   * 并发安全：先对场次行加排他锁（FOR UPDATE），同一场次的报名/退局被串行化，
   * 不会出现两人同时抢到同一个空位；(session_id, player_name) 唯一约束兜底重复报名。
   */
  @Transactional
  public RegisterResponse register(Long sessionId, String rawPlayerName) {
    String playerName = normalizePlayerName(rawPlayerName);
    Session session = requireSessionForUpdate(sessionId);

    Registration existing = registrationMapper.findBySessionAndPlayer(sessionId, playerName);
    if (existing != null) {
      if (Registration.STATUS_SEATED.equals(existing.getStatus())) {
        return new RegisterResponse(false, Registration.STATUS_SEATED,
            "你已报名该场次，座位已为你保留，请勿重复提交。", 0, toView(session));
      }
      int ahead = registrationMapper.countWaitingAhead(sessionId, existing.getId());
      return new RegisterResponse(false, Registration.STATUS_WAITING,
          "你已报名该场次，正在候补第 " + (ahead + 1) + " 位，前面还有 " + ahead + " 人等待。", ahead, toView(session));
    }

    int seatedCount = registrationMapper.countSeated(sessionId);
    if (seatedCount < session.getCapacity()) {
      insertRegistration(sessionId, playerName, Registration.STATUS_SEATED);
      return new RegisterResponse(true, Registration.STATUS_SEATED,
          "报名成功，已为你占座（本场第 " + (seatedCount + 1) + " 位入座）。", 0, toView(session));
    }

    Registration waiting = insertRegistration(sessionId, playerName, Registration.STATUS_WAITING);
    int ahead = registrationMapper.countWaitingAhead(sessionId, waiting.getId());
    return new RegisterResponse(true, Registration.STATUS_WAITING,
        "本场已满员，已加入候补队列，你前面还有 " + ahead + " 人等待。", ahead, toView(session));
  }

  /**
   * 退局：已入座玩家退局后，按候补顺序补位最早等待的一人。
   * 场次行锁保证同一时刻只有一个退局事务在办理补位，且补位更新带 status='WAITING' 条件，
   * 因此多人同时退局时一个空位只会补一人，不会把同一座位补给两个候补玩家。
   */
  @Transactional
  public CancelResponse cancel(Long sessionId, String rawPlayerName) {
    String playerName = normalizePlayerName(rawPlayerName);
    Session session = requireSessionForUpdate(sessionId);

    Registration existing = registrationMapper.findBySessionAndPlayer(sessionId, playerName);
    if (existing == null) {
      throw new ApiException(404, "未找到玩家 " + playerName + " 在该场次的报名记录。");
    }

    boolean wasSeated = Registration.STATUS_SEATED.equals(existing.getStatus());
    registrationMapper.deleteById(existing.getId());

    if (!wasSeated) {
      return new CancelResponse(true, "已退出该场次的候补队列。", null, toView(session));
    }

    String promotedPlayer = null;
    String message;
    String event;
    Registration firstWaiting = registrationMapper.findFirstWaiting(sessionId);
    if (firstWaiting != null && registrationMapper.promoteToSeated(firstWaiting.getId()) == 1) {
      promotedPlayer = firstWaiting.getPlayerName();
      event = "玩家 " + playerName + " 退局，候补玩家 " + promotedPlayer + " 已按顺序补位。";
      message = "已办理退局，空出的座位已补位给候补玩家 " + promotedPlayer + "。";
    } else {
      event = "玩家 " + playerName + " 退局，座位暂时空缺。";
      message = "已办理退局，座位暂时空缺。";
    }
    sessionMapper.updateLastEvent(sessionId, event);
    session.setLastEvent(event);
    return new CancelResponse(true, message, promotedPlayer, toView(session));
  }

  private Session requireSessionForUpdate(Long sessionId) {
    Session session = sessionMapper.findByIdForUpdate(sessionId);
    if (session == null) {
      throw new ApiException(404, "场次不存在或已下架。");
    }
    return session;
  }

  private Registration insertRegistration(Long sessionId, String playerName, String status) {
    Registration registration = new Registration();
    registration.setSessionId(sessionId);
    registration.setPlayerName(playerName);
    registration.setStatus(status);
    // 唯一约束冲突会抛出 DuplicateKeyException，交由全局异常处理器返回 409，
    // 当前事务随之回滚（PostgreSQL 中语句失败后不能继续使用同一事务）。
    registrationMapper.insert(registration);
    return registration;
  }

  private String normalizePlayerName(String rawPlayerName) {
    String playerName = rawPlayerName == null ? "" : rawPlayerName.trim();
    if (playerName.isEmpty()) {
      throw new ApiException(400, "玩家昵称不能为空。");
    }
    if (playerName.length() > 80) {
      throw new ApiException(400, "玩家昵称不能超过80个字符。");
    }
    return playerName;
  }

  private SessionView toView(Session session) {
    List<Registration> seated = registrationMapper.findSeated(session.getId());
    List<Registration> waiting = registrationMapper.findWaiting(session.getId());

    List<PlayerView> seatedPlayers = seated.stream()
        .map(registration -> new PlayerView(registration.getPlayerName()))
        .toList();
    List<WaitingPlayerView> waitingPlayers = new ArrayList<>();
    for (int index = 0; index < waiting.size(); index++) {
      waitingPlayers.add(new WaitingPlayerView(waiting.get(index).getPlayerName(), index + 1, index));
    }

    return new SessionView(
        session.getId(),
        session.getTitle(),
        session.getScriptName(),
        session.getDmName(),
        session.getStartTime(),
        session.getCapacity(),
        seated.size(),
        session.getCapacity() - seated.size(),
        waiting.size(),
        session.getLastEvent(),
        seatedPlayers,
        waitingPlayers);
  }
}
