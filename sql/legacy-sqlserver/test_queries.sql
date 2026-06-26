-- ============================================================
-- PeerReview 课程作业互评平台 · 测试查询用例
-- 覆盖：CRUD 验证 / 业务查询 / 统计报表 / 异常检测
-- ============================================================

USE softWare;
GO

-- ════════════════════════════════════════════════════════
-- TC-01: 查询所有用户按角色分类
-- ════════════════════════════════════════════════════════
SELECT r.role_name, COUNT(u.user_id) AS user_count
FROM dbo.roles r
LEFT JOIN dbo.users u ON r.role_id = u.role_id
GROUP BY r.role_name
ORDER BY r.role_id;
GO

-- ════════════════════════════════════════════════════════
-- TC-02: 查询某课程的所有学生（课程ID=1）
-- ════════════════════════════════════════════════════════
SELECT u.user_id, u.real_name, u.email, ce.role_in_course
FROM dbo.course_enrollments ce
JOIN dbo.users u ON ce.user_id = u.user_id
WHERE ce.course_id = 1 AND ce.role_in_course = 'student'
ORDER BY u.user_id;
GO

-- ════════════════════════════════════════════════════════
-- TC-03: 查询某教师负责的课程
-- ════════════════════════════════════════════════════════
SELECT c.course_id, c.course_name, c.course_code, c.semester,
       (SELECT COUNT(*) FROM dbo.course_enrollments ce WHERE ce.course_id = c.course_id AND ce.role_in_course = 'student') AS student_count
FROM dbo.courses c
WHERE c.teacher_id = 2;
GO

-- ════════════════════════════════════════════════════════
-- TC-04: 查询某作业的提交情况（作业ID=1）
-- ════════════════════════════════════════════════════════
SELECT a.title AS assignment_title,
       COUNT(s.submission_id) AS total_submissions,
       SUM(CASE WHEN s.status = 'submitted' THEN 1 ELSE 0 END) AS on_time,
       SUM(CASE WHEN s.status = 'late' THEN 1 ELSE 0 END) AS late
FROM dbo.assignments a
LEFT JOIN dbo.submissions s ON a.assignment_id = s.assignment_id
WHERE a.assignment_id = 1
GROUP BY a.title;
GO

-- ════════════════════════════════════════════════════════
-- TC-05: 查询某个学生的所有提交
-- ════════════════════════════════════════════════════════
SELECT a.title, s.file_name, s.status, s.submitted_at, s.similarity_pct
FROM dbo.submissions s
JOIN dbo.assignments a ON s.assignment_id = a.assignment_id
WHERE s.student_id = 6
ORDER BY s.submitted_at DESC;
GO

-- ════════════════════════════════════════════════════════
-- TC-06: 查询某学生的互评任务清单（待评/已评）
-- ════════════════════════════════════════════════════════
SELECT pra.pra_id,
       a.title AS assignment_title,
       us.real_name AS submitter_name,
       s.file_name,
       pra.status,
       r.total_score,
       pra.assigned_at
FROM dbo.peer_review_assignments pra
JOIN dbo.submissions s    ON pra.submission_id = s.submission_id
JOIN dbo.assignments a    ON pra.assignment_id = a.assignment_id
JOIN dbo.users us         ON s.student_id = us.user_id
LEFT JOIN dbo.reviews r    ON pra.pra_id = r.pra_id
WHERE pra.reviewer_id = 6
ORDER BY pra.status, pra.assigned_at;
GO

-- ════════════════════════════════════════════════════════
-- TC-07: 查询某次互评的逐项得分详情
-- ════════════════════════════════════════════════════════
SELECT ri.item_name, ri.max_score, rs.score, rs.comment
FROM dbo.review_scores rs
JOIN dbo.rubric_items ri ON rs.rubric_item_id = ri.item_id
WHERE rs.review_id = 1
ORDER BY ri.sort_order;
GO

-- ════════════════════════════════════════════════════════
-- TC-08: 查询某作业的互评完成进度
-- ════════════════════════════════════════════════════════
SELECT a.title,
       COUNT(pra.pra_id) AS total_assigned,
       SUM(CASE WHEN pra.status = 'completed' THEN 1 ELSE 0 END) AS completed,
       SUM(CASE WHEN pra.status = 'pending' THEN 1 ELSE 0 END) AS pending
FROM dbo.peer_review_assignments pra
JOIN dbo.assignments a ON pra.assignment_id = a.assignment_id
WHERE a.assignment_id = 1
GROUP BY a.title;
GO

-- ════════════════════════════════════════════════════════
-- TC-09: 查询每个学生在作业1中的获得的总分（根据互评均分）
-- ════════════════════════════════════════════════════════
SELECT u.user_id, u.real_name,
       COUNT(r.review_id) AS review_count,
       ISNULL(AVG(r.total_score), 0) AS avg_score,
       ISNULL(MIN(r.total_score), 0) AS min_score,
       ISNULL(MAX(r.total_score), 0) AS max_score
FROM dbo.users u
JOIN dbo.submissions s ON u.user_id = s.student_id AND s.assignment_id = 1
JOIN dbo.peer_review_assignments pra ON pra.submission_id = s.submission_id AND pra.status = 'completed'
JOIN dbo.reviews r ON pra.pra_id = r.pra_id
WHERE u.role_id = 4
GROUP BY u.user_id, u.real_name
ORDER BY avg_score DESC;
GO

-- ════════════════════════════════════════════════════════
-- TC-10: 互评一致性检测 —— 找出评分方差异常的学生
-- （同一份提交收到多个评分，标准差过大说明评分不一致）
-- ════════════════════════════════════════════════════════
SELECT u.real_name AS submitter,
       STDEV(r.total_score) AS score_stddev,
       COUNT(r.review_id) AS review_count,
       MIN(r.total_score) AS min_score,
       MAX(r.total_score) AS max_score,
       CASE
           WHEN STDEV(r.total_score) > 15 THEN N'⚠ 异常（评分分歧大）'
           ELSE N'正常'
       END AS alert
FROM dbo.submissions s
JOIN dbo.users u ON s.student_id = u.user_id
JOIN dbo.peer_review_assignments pra ON s.submission_id = pra.submission_id AND pra.status = 'completed'
JOIN dbo.reviews r ON pra.pra_id = r.pra_id
WHERE s.assignment_id = 1
GROUP BY s.submission_id, u.real_name
HAVING COUNT(r.review_id) >= 2
ORDER BY score_stddev DESC;
GO

-- ════════════════════════════════════════════════════════
-- TC-11: 分数分布统计（作业1）
-- ════════════════════════════════════════════════════════
SELECT CASE
           WHEN total_score >= 90 THEN N'90-100'
           WHEN total_score >= 80 THEN N'80-89'
           WHEN total_score >= 70 THEN N'70-79'
           WHEN total_score >= 60 THEN N'60-69'
           ELSE N'<60'
       END AS score_range,
       COUNT(*) AS count
FROM dbo.reviews
GROUP BY CASE
           WHEN total_score >= 90 THEN N'90-100'
           WHEN total_score >= 80 THEN N'80-89'
           WHEN total_score >= 70 THEN N'70-79'
           WHEN total_score >= 60 THEN N'60-69'
           ELSE N'<60'
       END
ORDER BY score_range;
GO

-- ════════════════════════════════════════════════════════
-- TC-12: 查询待处理的申诉
-- ════════════════════════════════════════════════════════
SELECT a.appeal_id,
       us.real_name AS student_name,
       uh.real_name AS handler_name,
       a.reason,
       a.status,
       a.handler_response,
       a.created_at,
       a.resolved_at
FROM dbo.appeals a
JOIN dbo.users us ON a.student_id = us.user_id
LEFT JOIN dbo.users uh ON a.handler_id = uh.user_id
WHERE a.status = 'pending'
ORDER BY a.created_at;
GO

-- ════════════════════════════════════════════════════════
-- TC-13: 查询某学生的未读通知
-- ════════════════════════════════════════════════════════
SELECT notification_id, title, content, created_at
FROM dbo.notifications
WHERE user_id = 6 AND is_read = 0
ORDER BY created_at DESC;
GO

-- ════════════════════════════════════════════════════════
-- TC-14: 模拟——查重检测
-- （查询与某提交相似度超过阈值(30%)的其他提交，用于作弊预警）
-- ════════════════════════════════════════════════════════
SELECT s1.submission_id AS submission_1,
       u1.real_name    AS student_1,
       s2.submission_id AS submission_2,
       u2.real_name    AS student_2,
       s2.similarity_pct
FROM dbo.submissions s1
CROSS APPLY (
    SELECT TOP 1 s2.submission_id, s2.student_id, s2.similarity_pct
    FROM dbo.submissions s2
    WHERE s2.assignment_id = s1.assignment_id
      AND s2.submission_id <> s1.submission_id
    ORDER BY ABS(CHECKSUM(s2.content_hash) - CHECKSUM(s1.content_hash))
) s2
JOIN dbo.users u1 ON s1.student_id = u1.user_id
JOIN dbo.users u2 ON s2.student_id = u2.user_id
WHERE s1.assignment_id = 1
  AND s2.similarity_pct > 30
ORDER BY s2.similarity_pct DESC;
GO

-- ════════════════════════════════════════════════════════
-- TC-15: 完整的学生作业成绩单（含互评均分 + 申诉调整）
-- ════════════════════════════════════════════════════════
WITH raw_scores AS (
    SELECT s.student_id,
           s.submission_id,
           AVG(r.total_score) AS avg_peer_score
    FROM dbo.submissions s
    JOIN dbo.peer_review_assignments pra ON s.submission_id = pra.submission_id AND pra.status = 'completed'
    JOIN dbo.reviews r ON pra.pra_id = r.pra_id
    WHERE s.assignment_id = 1
    GROUP BY s.student_id, s.submission_id
),
adjusted AS (
    SELECT student_id, MAX(adjusted_score) AS adjusted_score
    FROM dbo.appeals
    WHERE status = 'accepted'
    GROUP BY student_id
)
SELECT u.real_name,
       rs.avg_peer_score,
       ISNULL(adj.adjusted_score, rs.avg_peer_score) AS final_score,
       CASE WHEN adj.adjusted_score IS NOT NULL THEN N'已申诉调整' ELSE N'-' END AS note
FROM raw_scores rs
JOIN dbo.users u ON rs.student_id = u.user_id
LEFT JOIN adjusted adj ON rs.student_id = adj.student_id
ORDER BY final_score DESC;
GO

PRINT '>> 全部测试查询执行完毕 (15个用例)';
GO
