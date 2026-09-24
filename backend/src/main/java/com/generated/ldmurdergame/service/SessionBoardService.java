package com.generated.ldmurdergame.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.generated.ldmurdergame.exception.ApiException;
import com.generated.ldmurdergame.mapper.RegistrationMapper;
import com.generated.ldmurdergame.mapper.SessionMapper;
import com.generated.ldmurdergame.model.Session;
import com.generated.ldmurdergame.model.SessionActionResult;
import com.generated.ldmurdergame.model.SessionRegistration;
import com.generated.ldmurdergame.model.SessionView;

/**
 * 场次报名 / 候补 / 退局补位。
 *
 * 并发模型：同一场次的报名与退局在本服务内按场次加锁串行执行，锁内再开事务，
 * 因此“空位计数 + 插入报名”以及“退局 + 候补补位”不会交错：
 * 两个玩家同时退局时，每个退局事务各自只补入队首一名候补，一个空位只补一人。
 */
@Service
public class SessionBoardService {
  private final SessionMapper sessionMapper;
  private final RegistrationMapper registrationMapper;
  private final TransactionTemplate transactionTemplate;

  /** 每个场次一把锁，避免不同场次之间互相阻塞。 */
  private final Map<Long, Object> sessionLocks = new ConcurrentHashMap<>();

  public SessionBoardService(SessionMapper sessionMapper,
                             RegistrationMapper registrationMapper,
                             PlatformTransactionManager transactionManager) {
    this.sessionMapper = sessionMapper;
    this.registrationMapper = registrationMapper;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  public List<SessionView> listSessions() {
    List<SessionView> views = new ArrayList<>();
    for (Session session : sessionMapper.findAll()) {
      views.add(buildView(session, null));
    }
    return views;
  }

  public SessionView getSession(Long sessionId) {
    Session session = requireSession(sessionId);
    return buildView(session, null);
  }

  public SessionActionResult register(Long sessionId, String rawPlayerName) {
    String playerName = normalizeName(rawPlayerName);
    Object lock = sessionLocks.computeIfAbsent(sessionId, key -> new Object());
    synchronized (lock) {
      return transactionTemplate.execute(status -> {
        Session session = requireSession(sessionId);

        SessionRegistration existing = registrationMapper.find(sessionId, playerName);
        if (existing != null) {
          if (SessionRegistration.STATUS_SEATED.equals(existing.getStatus())) {
            throw new ApiException("玩家「" + playerName + "」已报名该场次，无需重复报名。");
          }
          int ahead = registrationMapper.countWaitingAhead(sessionId, existing.getId());
          throw new ApiException("玩家「" + playerName + "」已在候补队列中，前面还有 " + ahead + " 人，请耐心等待补位。");
        }

        int seated = registrationMapper.countByStatus(sessionId, SessionRegistration.STATUS_SEATED);
        SessionRegistration registration = new SessionRegistration();
        registration.setSessionId(sessionId);
        registration.setPlayerName(playerName);

        String message;
        if (seated < session.getCapacity()) {
          registration.setStatus(SessionRegistration.STATUS_SEATED);
          registration.setWaitPosition(null);
          message = "报名成功，已为「" + playerName + "」占座。";
        } else {
          registration.setStatus(SessionRegistration.STATUS_WAITING);
          int waitingCount = registrationMapper.countByStatus(sessionId, SessionRegistration.STATUS_WAITING);
          registration.setWaitPosition(waitingCount + 1);
          message = "本场已满员，「" + playerName + "」已进入候补，前面等待 " + waitingCount + " 人。";
        }
        registrationMapper.insert(registration);

        SessionView view = buildView(session, playerName);
        String playerStatus = view.playerStatus();
        Integer waitingPosition = view.waitingPosition();
        return new SessionActionResult(message, playerStatus, waitingPosition, view);
      });
    }
  }

  public SessionActionResult cancel(Long sessionId, String rawPlayerName) {
    String playerName = normalizeName(rawPlayerName);
    Object lock = sessionLocks.computeIfAbsent(sessionId, key -> new Object());
    synchronized (lock) {
      return transactionTemplate.execute(status -> {
        Session session = requireSession(sessionId);

        SessionRegistration existing = registrationMapper.find(sessionId, playerName);
        if (existing == null) {
          throw new ApiException("玩家「" + playerName + "」没有该场次的报名记录。", HttpStatus.NOT_FOUND);
        }

        String message;
        if (SessionRegistration.STATUS_WAITING.equals(existing.getStatus())) {
          // 候补退出不产生空位，无需补位
          registrationMapper.deleteById(existing.getId());
          resequenceWaiting(sessionId);
          message = "玩家「" + playerName + "」已退出候补队列。";
        } else {
          registrationMapper.deleteById(existing.getId());

          // 退局释放出一个空位：无论有多少人同时退局，每个退局事务最多只补队首一人
          SessionRegistration next = registrationMapper.findFirstWaiting(sessionId);
          if (next != null) {
            registrationMapper.promoteToSeated(next.getId());
            resequenceWaiting(sessionId);
            LocalDateTime promotedAt = LocalDateTime.now();
            String note = "「" + playerName + "」退局，候补第 1 位「" + next.getPlayerName() + "」补位成功";
            sessionMapper.updateLastPromotion(sessionId, next.getPlayerName(), promotedAt, note);
            session.setLastPromotedPlayer(next.getPlayerName());
            session.setLastPromotedAt(promotedAt);
            session.setLastPromotionNote(note);
            message = "玩家「" + playerName + "」已退局，候补队首「" + next.getPlayerName() + "」已自动补位。";
          } else {
            String note = "「" + playerName + "」退局，暂无候补玩家，空位保留";
            LocalDateTime promotedAt = LocalDateTime.now();
            sessionMapper.updateLastPromotion(sessionId, null, promotedAt, note);
            session.setLastPromotedPlayer(null);
            session.setLastPromotedAt(promotedAt);
            session.setLastPromotionNote(note);
            message = "玩家「" + playerName + "」已退局，当前没有候补玩家，空位保留待报名。";
          }
        }

        SessionView view = buildView(session, playerName);
        return new SessionActionResult(message, view.playerStatus(), view.waitingPosition(), view);
      });
    }
  }

  /** 候补队列按入队先后（id 升序）重排位置，从 1 开始。 */
  private void resequenceWaiting(Long sessionId) {
    List<SessionRegistration> waiting = registrationMapper.findWaiting(sessionId);
    int position = 1;
    for (SessionRegistration registration : waiting) {
      if (registration.getWaitPosition() == null || registration.getWaitPosition() != position) {
        registrationMapper.updateWaitPosition(registration.getId(), position);
      }
      position++;
    }
  }

  private String normalizeName(String rawPlayerName) {
    if (rawPlayerName == null) {
      throw new ApiException("玩家昵称不能为空。");
    }
    String playerName = rawPlayerName.trim();
    if (playerName.isEmpty()) {
      throw new ApiException("玩家昵称不能为空。");
    }
    if (playerName.length() > 80) {
      throw new ApiException("玩家昵称最长 80 个字符。");
    }
    return playerName;
  }

  private Session requireSession(Long sessionId) {
    Session session = sessionMapper.findById(sessionId);
    if (session == null) {
      throw new ApiException("场次不存在或已下架。", HttpStatus.NOT_FOUND);
    }
    return session;
  }

  private SessionView buildView(Session session, String focusPlayer) {
    int seated = registrationMapper.countByStatus(session.getId(), SessionRegistration.STATUS_SEATED);
    List<SessionRegistration> waiting = registrationMapper.findWaiting(session.getId());

    List<SessionView.WaitingPlayer> waitingPlayers = new ArrayList<>();
    int index = 1;
    for (SessionRegistration registration : waiting) {
      waitingPlayers.add(new SessionView.WaitingPlayer(registration.getPlayerName(), index));
      index++;
    }

    String playerStatus = null;
    Integer waitingPosition = null;
    if (focusPlayer != null) {
      SessionRegistration self = registrationMapper.find(session.getId(), focusPlayer);
      if (self != null) {
        playerStatus = self.getStatus();
        if (SessionRegistration.STATUS_WAITING.equals(self.getStatus())) {
          waitingPosition = waitingPlayers.stream()
            .filter(item -> item.playerName().equals(focusPlayer))
            .map(SessionView.WaitingPlayer::position)
            .findFirst()
            .orElse(null);
        }
      }
    }

    int freeSeats = Math.max(0, session.getCapacity() - seated);
    return new SessionView(
      session.getId(),
      session.getTitle(),
      session.getStartTime(),
      session.getCapacity(),
      seated,
      freeSeats,
      freeSeats == 0,
      waiting.size(),
      playerStatus,
      waitingPosition,
      session.getLastPromotedPlayer(),
      session.getLastPromotedAt(),
      session.getLastPromotionNote(),
      registrationMapper.findSeatedPlayers(session.getId()),
      waitingPlayers
    );
  }
}
