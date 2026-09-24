-- 本地 H2 演示场次，与 database/init.sql 的种子数据保持一致
INSERT INTO sessions (title, start_time, capacity)
SELECT '14:00 场 · 窗边的女人', DATEADD('HOUR', 4, CURRENT_TIMESTAMP), 6
WHERE NOT EXISTS (SELECT 1 FROM sessions WHERE title = '14:00 场 · 窗边的女人');

INSERT INTO sessions (title, start_time, capacity)
SELECT '19:00 场 · 年轮', DATEADD('HOUR', 9, CURRENT_TIMESTAMP), 8
WHERE NOT EXISTS (SELECT 1 FROM sessions WHERE title = '19:00 场 · 年轮');

INSERT INTO sessions (title, start_time, capacity)
SELECT '21:30 场 · 雪乡连环杀人事件', DATEADD('HOUR', 12, CURRENT_TIMESTAMP), 6
WHERE NOT EXISTS (SELECT 1 FROM sessions WHERE title = '21:30 场 · 雪乡连环杀人事件');
