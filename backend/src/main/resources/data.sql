INSERT INTO sessions (title, script_name, dm_name, start_time, capacity, last_event)
VALUES
  ('周五晚场 · 硬核推理', '年轮', '老K', '今天 19:00', 6, '暂无补位记录'),
  ('周六下午场 · 情感沉浸', '金陵有座东君书院', '阿离', '明天 14:00', 7, '暂无补位记录'),
  ('周六晚场 · 欢乐机制', '来电', '北辰', '明天 19:30', 8, '暂无补位记录');

INSERT INTO registrations (session_id, player_name, status)
VALUES
  (1, '林晚', 'SEATED'),
  (1, '周野', 'SEATED'),
  (1, '苏离', 'SEATED'),
  (1, '陈默', 'SEATED');
