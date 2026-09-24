-- 剧本杀场次报名 / 候补补位 初始化脚本（PostgreSQL）

-- 场次：记录座位容量与最近一次补位结果，供总览页实时展示
CREATE TABLE IF NOT EXISTS sessions (
  id              SERIAL PRIMARY KEY,
  title           VARCHAR(120) NOT NULL,
  start_time      TIMESTAMP NOT NULL,
  capacity        INTEGER NOT NULL CHECK (capacity > 0),
  last_promoted_player VARCHAR(80),
  last_promoted_at     TIMESTAMP,
  last_promotion_note  VARCHAR(255)
);

-- 报名：同一玩家在同一场次仅保留一条记录（ON CONFLICT 依赖该唯一约束）
CREATE TABLE IF NOT EXISTS session_registrations (
  id           SERIAL PRIMARY KEY,
  session_id   INTEGER NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  player_name  VARCHAR(80) NOT NULL,
  status       VARCHAR(16) NOT NULL,
  wait_position INTEGER,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (session_id, player_name)
);

-- 候补队列按该索引顺序出队（status 在前，便于快速过滤）
CREATE INDEX IF NOT EXISTS idx_registrations_session_waiting
  ON session_registrations (session_id, status, id);

-- 演示场次：可直接在总览页体验占座、满员候补、退局补位
INSERT INTO sessions (title, start_time, capacity)
SELECT '14:00 场 · 窗边的女人', CURRENT_TIMESTAMP + INTERVAL '4 HOUR', 6
WHERE NOT EXISTS (SELECT 1 FROM sessions WHERE title = '14:00 场 · 窗边的女人');

INSERT INTO sessions (title, start_time, capacity)
SELECT '19:00 场 · 年轮', CURRENT_TIMESTAMP + INTERVAL '9 HOUR', 8
WHERE NOT EXISTS (SELECT 1 FROM sessions WHERE title = '19:00 场 · 年轮');

INSERT INTO sessions (title, start_time, capacity)
SELECT '21:30 场 · 雪乡连环杀人事件', CURRENT_TIMESTAMP + INTERVAL '12 HOUR', 6
WHERE NOT EXISTS (SELECT 1 FROM sessions WHERE title = '21:30 场 · 雪乡连环杀人事件');
