-- ============================================================
-- PeerReview · 测试数据 v2（确定性、多场景）
-- 要求：中文 NVARCHAR 使用 N'...'；Docker 执行需 sqlcmd -f 65001
-- ============================================================
-- 用户 ID 约定（IDENTITY 顺序插入）：
--   1 admin01 | 2 teacher01 | 3 teacher02 | 4 ta01 | 5 ta02
--   6–37  stu2026001–stu2026032（32 人）
--
-- 演示场景速查：
--   作业1 SRS     → 已派评，互评大部分完成，可查统计/申诉/讨论
--   作业2 设计    → 仅提交，未派评（教师可点「分配互评」）
--   作业3         → 草稿（教师可发布）
--   数据结构课    → 第二门课、另一教师
--   stu2026001    → 互评均分偏低，有【待处理申诉】
--   stu2026005/6  → 高相似度提交（simhash 演示）
--   stu2026031/32 → 未交作业1（缺交样本）
--   locked_stu    → 停用账号（管理员面板）
-- ============================================================

USE softWare;
GO

SET NOCOUNT ON;
GO

-- ── 1. 角色 ──────────────────────────────────────────────
INSERT INTO dbo.roles (role_name, description) VALUES
('admin',   N'系统管理员'),
('teacher', N'授课教师'),
('ta',      N'课程助教'),
('student', N'学生');
GO

-- ── 2. 用户 ──────────────────────────────────────────────
INSERT INTO dbo.users (username, password_hash, real_name, nickname, email, student_id, class_name, role_id, is_active) VALUES
('admin',   '123456', N'赵管理',   N'Admin',   'admin01@bjfu.edu.cn',   NULL,       NULL,           1, 1);

INSERT INTO dbo.users (username, password_hash, real_name, nickname, email, student_id, class_name, role_id, is_active) VALUES
('teacher01', '123456', N'李教授',   N'李教授',   'liprof@bjfu.edu.cn',    NULL,       NULL,           2, 1),
('teacher02', '123456', N'王副教授', N'王副教授', 'wangap@bjfu.edu.cn',    NULL,       NULL,           2, 1);

INSERT INTO dbo.users (username, password_hash, real_name, nickname, email, student_id, class_name, role_id, is_active) VALUES
('ta01',      '123456', N'陈助教',   N'陈助教',   'chenta@bjfu.edu.cn',    NULL,       NULL,           3, 1),
('ta02',      '123456', N'刘助教',   N'刘助教',   'liuta@bjfu.edu.cn',     NULL,       NULL,           3, 1);

INSERT INTO dbo.users (username, password_hash, real_name, nickname, email, student_id, class_name, role_id, is_active) VALUES
('stu2026001','123456', N'张晓明', N'晓明', 'zhangxm@bjfu.edu.cn',  '2026001', N'软工2301', 4, 1),
('stu2026002','123456', N'李婷婷', N'婷婷', 'litt@bjfu.edu.cn',     '2026002', N'软工2301', 4, 1),
('stu2026003','123456', N'王浩然', N'浩然', 'wanghr@bjfu.edu.cn',   '2026003', N'软工2301', 4, 1),
('stu2026004','123456', N'赵思雨', N'思雨', 'zhaosy@bjfu.edu.cn',   '2026004', N'软工2301', 4, 1),
('stu2026005','123456', N'刘子涵', N'子涵', 'liuzh@bjfu.edu.cn',    '2026005', N'软工2301', 4, 1),
('stu2026006','123456', N'陈一鸣', N'一鸣', 'chenym@bjfu.edu.cn',   '2026006', N'软工2301', 4, 1),
('stu2026007','123456', N'杨雨桐', N'雨桐', 'yangyt@bjfu.edu.cn',   '2026007', N'软工2301', 4, 1),
('stu2026008','123456', N'周文博', N'文博', 'zhouwb@bjfu.edu.cn',   '2026008', N'软工2301', 4, 1),
('stu2026009','123456', N'吴佳琪', N'佳琪', 'wujq@bjfu.edu.cn',     '2026009', N'软工2301', 4, 1),
('stu2026010','123456', N'郑浩宇', N'浩宇', 'zhenghy@bjfu.edu.cn',  '2026010', N'软工2301', 4, 1),
('stu2026011','123456', N'钱思远', N'思远', 'qiansy@bjfu.edu.cn',   '2026011', N'软工2301', 4, 1),
('stu2026012','123456', N'孙雨萱', N'雨萱', 'sunyx@bjfu.edu.cn',    '2026012', N'软工2301', 4, 1),
('stu2026013','123456', N'马俊杰', N'俊杰', 'majj@bjfu.edu.cn',     '2026013', N'软工2301', 4, 1),
('stu2026014','123456', N'朱晓雯', N'晓雯', 'zhuxw@bjfu.edu.cn',    '2026014', N'软工2301', 4, 1),
('stu2026015','123456', N'胡志强', N'志强', 'huzq@bjfu.edu.cn',     '2026015', N'软工2301', 4, 1),
('stu2026016','123456', N'林诗涵', N'诗涵', 'linsh@bjfu.edu.cn',    '2026016', N'软工2301', 4, 1),
('stu2026017','123456', N'何雨霏', N'雨霏', 'heyf@bjfu.edu.cn',     '2026017', N'软工2301', 4, 1),
('stu2026018','123456', N'郭子轩', N'子轩', 'guozx@bjfu.edu.cn',    '2026018', N'软工2301', 4, 1),
('stu2026019','123456', N'高思琪', N'思琪', 'gaosq@bjfu.edu.cn',    '2026019', N'软工2301', 4, 1),
('stu2026020','123456', N'罗一航', N'一航', 'luoyh@bjfu.edu.cn',    '2026020', N'软工2301', 4, 1),
('stu2026021','123456', N'梁婉清', N'婉清', 'liangwq@bjfu.edu.cn',  '2026021', N'软工2302', 4, 1),
('stu2026022','123456', N'宋志远', N'志远', 'songzy@bjfu.edu.cn',   '2026022', N'软工2302', 4, 1),
('stu2026023','123456', N'唐雨薇', N'雨薇', 'tangyw@bjfu.edu.cn',   '2026023', N'软工2302', 4, 1),
('stu2026024','123456', N'韩明哲', N'明哲', 'hanmz@bjfu.edu.cn',    '2026024', N'软工2302', 4, 1),
('stu2026025','123456', N'冯佳琳', N'佳琳', 'fengjl@bjfu.edu.cn',   '2026025', N'软工2302', 4, 1),
('stu2026026','123456', N'董子涵', N'子涵', 'dongzh@bjfu.edu.cn',   '2026026', N'软工2302', 4, 1),
('stu2026027','123456', N'袁博文', N'博文', 'yuanbw@bjfu.edu.cn',   '2026027', N'软工2302', 4, 1),
('stu2026028','123456', N'邓雨晴', N'雨晴', 'dengyq@bjfu.edu.cn',   '2026028', N'软工2302', 4, 1),
('stu2026029','123456', N'许浩宸', N'浩宸', 'xuhc@bjfu.edu.cn',     '2026029', N'软工2302', 4, 1),
('stu2026030','123456', N'傅思源', N'思源', 'fusy@bjfu.edu.cn',     '2026030', N'软工2302', 4, 1),
('stu2026031','123456', N'沈乐天', N'乐天', 'shenlt@bjfu.edu.cn',   '2026031', N'软工2302', 4, 1),
('stu2026032','123456', N'曾雨彤', N'雨彤', 'zengyt@bjfu.edu.cn',   '2026032', N'软工2302', 4, 1);

INSERT INTO dbo.users (username, password_hash, real_name, email, role_id, is_active) VALUES
('locked_stu', '123456', N'已停用用户', 'locked@bjfu.edu.cn', 4, 0);
GO

-- ── 3. 课程 ──────────────────────────────────────────────
INSERT INTO dbo.courses (course_name, course_code, description, teacher_id, semester, is_archived) VALUES
(N'软件工程', 'CS310-01',
 N'专业核心课：需求分析、系统设计、编码测试与项目管理。主演示课程。',
 2, N'2026春', 0),
(N'数据结构', 'CS210-01',
 N'算法与数据结构基础。用于演示多教师、多课程隔离。',
 3, N'2026春', 0);
GO

-- ── 4. 选课 ──────────────────────────────────────────────
-- 软件工程：32 学生 + 2 助教
DECLARE @uid INT = 6;
WHILE @uid <= 37
BEGIN
    INSERT INTO dbo.course_enrollments (course_id, user_id, role_in_course) VALUES (1, @uid, 'student');
    SET @uid = @uid + 1;
END;

INSERT INTO dbo.course_enrollments (course_id, user_id, role_in_course) VALUES
(1, 4, 'ta'), (1, 5, 'ta');

-- 数据结构：前 12 名学生
SET @uid = 6;
WHILE @uid <= 17
BEGIN
    INSERT INTO dbo.course_enrollments (course_id, user_id, role_in_course) VALUES (2, @uid, 'student');
    SET @uid = @uid + 1;
END;
GO

-- ── 5. 作业 ──────────────────────────────────────────────
INSERT INTO dbo.assignments (course_id, title, description, due_date, review_due_date,
    file_types, max_file_size_mb, peer_review_count, status, created_by) VALUES
(1, N'第一次作业：需求分析文档（SRS）',
 N'选择真实场景编写 SRS，含功能/非功能需求、用例图与类图初稿。',
 '2026-03-20 23:59:59', '2026-03-27 23:59:59', 'pdf,doc,docx', 20, 3, 'published', 2),

(1, N'第二次作业：系统设计文档',
 N'基于作业1场景完成概要设计与详细设计，含架构图、ER 图、核心类图。',
 '2026-05-10 23:59:59', '2026-05-17 23:59:59', 'pdf,doc,docx,zip', 50, 3, 'published', 2),

(1, N'第三次作业：测试计划（草稿）',
 N'撰写系统测试计划与用例表。当前为草稿，用于演示发布流程。',
 '2026-06-01 23:59:59', '2026-06-08 23:59:59', 'pdf,docx', 10, 2, 'draft', 2),

(2, N'数据结构作业1：线性表应用',
 N'实现并分析顺序表/链表的插入删除，提交实验报告。',
 '2026-04-15 23:59:59', '2026-04-22 23:59:59', 'pdf', 10, 2, 'published', 3);
GO

-- ── 6. 量规（作业1、2、4 已配置）──────────────────────────
INSERT INTO dbo.rubrics (assignment_id) VALUES (1), (2), (4);
GO

INSERT INTO dbo.rubric_items (rubric_id, item_name, max_score, weight, description, sort_order) VALUES
(1, N'需求完整性',       25, 1.0, N'功能需求覆盖是否完整',           1),
(1, N'非功能需求',       15, 1.0, N'性能、安全、可用性描述',         2),
(1, N'用例图规范性',     20, 1.0, N'UML 用例图是否符合规范',         3),
(1, N'文档结构与排版',   15, 1.0, N'章节结构与排版',                 4),
(1, N'创新性与分析深度', 25, 1.0, N'业务分析深度与创新点',           5);

INSERT INTO dbo.rubric_items (rubric_id, item_name, max_score, weight, description, sort_order) VALUES
(2, N'体系结构合理性',   20, 1.0, N'分层与架构风格',                 1),
(2, N'模块划分与接口',   20, 1.0, N'模块粒度与接口定义',             2),
(2, N'数据库设计',       25, 1.0, N'ER 图与表结构设计',              3),
(2, N'类图与详细设计',   20, 1.0, N'核心类图与设计模式',             4),
(2, N'文档质量与完整性', 15, 1.0, N'文档完整性',                     5);

INSERT INTO dbo.rubric_items (rubric_id, item_name, max_score, weight, description, sort_order) VALUES
(3, N'算法正确性',       40, 1.0, N'线性表操作实现正确',             1),
(3, N'复杂度分析',       30, 1.0, N'时间与空间复杂度分析',           2),
(3, N'实验报告质量',     30, 1.0, N'实验步骤与结果说明',             3);
GO

-- ── 7. 作业1 提交（30 人，缺 stu3031/3032）──────────────
DECLARE @stu INT = 6;
DECLARE @seq INT = 0;
WHILE @stu <= 35
BEGIN
    SET @seq = @seq + 1;
    DECLARE @hash VARCHAR(64) = LOWER(CONVERT(VARCHAR(32), HASHBYTES('MD5', 'srs-a1-' + CAST(@stu AS VARCHAR(10))), 2));
    DECLARE @sim DECIMAL(5,2) = 5.0 + (@seq % 8) * 2.5;
    DECLARE @txt NVARCHAR(MAX) = CONCAT(N'【SRS】学号', @stu - 5, N'：图书馆管理系统需求分析，含借还书、检索、权限管理用例。');
    DECLARE @fname NVARCHAR(200) = CONCAT(N'SRS_学号', RIGHT('00' + CAST(@stu - 5 AS VARCHAR(3)), 2), N'.pdf');
    DECLARE @late VARCHAR(20) = CASE WHEN @stu IN (34, 35) THEN 'late' ELSE 'submitted' END;

    -- 高相似度样本：stu2026005/6（user_id 10/11）
    IF @stu IN (10, 11)
    BEGIN
        SET @hash = 'a1b2c3d4e5f6789012345678abcdef01';
        SET @sim = CASE WHEN @stu = 15 THEN 92.5 ELSE 88.0 END;
        SET @txt = N'【SRS】图书馆管理系统：高度相似的模板化需求正文，用于 simhash 查重演示。';
    END

    INSERT INTO dbo.submissions (assignment_id, student_id, file_path, file_name, content_text,
        content_hash, similarity_pct, status, submitted_at)
    VALUES (1, @stu,
        CONCAT('uploads/demo/a1/stu', @stu, '.pdf'),
        @fname, @txt, @hash, @sim, @late,
        DATEADD(HOUR, -(@seq % 36), '2026-03-20 20:00:00'));

    SET @stu = @stu + 1;
END;
GO

-- 教师终审（部分优秀作业）
UPDATE dbo.submissions SET final_score = 92.0, final_comment = N'需求完整、用例清晰，终审优秀。'
WHERE assignment_id = 1 AND student_id = 10;
UPDATE dbo.submissions SET final_score = 88.5, final_comment = N'结构规范，非功能需求可再细化。'
WHERE assignment_id = 1 AND student_id = 12;
UPDATE dbo.submissions SET final_score = 85.0, final_comment = N'申诉复核后确定的最终成绩。'
WHERE assignment_id = 1 AND student_id = 6;
GO

-- ── 8. 作业2 提交（18 人，未派评）────────────────────────
DECLARE @stu2 INT = 6;
DECLARE @seq2 INT = 0;
WHILE @stu2 <= 23
BEGIN
    SET @seq2 = @seq2 + 1;
    INSERT INTO dbo.submissions (assignment_id, student_id, file_path, file_name, content_text,
        content_hash, similarity_pct, status, submitted_at)
    VALUES (2, @stu2,
        CONCAT('uploads/demo/a2/stu', @stu2, '.pdf'),
        CONCAT(N'设计文档_学号', RIGHT('00' + CAST(@stu2 - 5 AS VARCHAR(3)), 2), N'.pdf'),
        CONCAT(N'【设计】学号', @stu2 - 5, N'：系统概要设计、模块划分与数据库 ER 图。'),
        LOWER(CONVERT(VARCHAR(32), HASHBYTES('MD5', 'design-a2-' + CAST(@stu2 AS VARCHAR(10))), 2)),
        3.0, 'submitted',
        DATEADD(HOUR, -(@seq2 % 24), '2026-07-10 18:00:00'));
    SET @stu2 = @stu2 + 1;
END;
GO

-- ── 9. 数据结构课 提交（8 人）────────────────────────────
DECLARE @stu3 INT = 6;
DECLARE @seq3 INT = 0;
WHILE @stu3 <= 13
BEGIN
    SET @seq3 = @seq3 + 1;
    INSERT INTO dbo.submissions (assignment_id, student_id, file_path, file_name, content_text,
        content_hash, similarity_pct, status, submitted_at)
    VALUES (4, @stu3,
        CONCAT('uploads/demo/ds/stu', @stu3, '.pdf'),
        CONCAT(N'线性表实验_学号', RIGHT('00' + CAST(@stu3 - 5 AS VARCHAR(3)), 2), N'.pdf'),
        N'顺序表与链表插入删除实验及复杂度分析。',
        LOWER(CONVERT(VARCHAR(32), HASHBYTES('MD5', 'ds-a4-' + CAST(@stu3 AS VARCHAR(10))), 2)),
        0, 'submitted',
        DATEADD(HOUR, -@seq3, '2026-07-15 12:00:00'));
    SET @stu3 = @stu3 + 1;
END;
GO

-- ── 10. 作业1 派评（30 份提交 × 3 评阅人，确定性轮转）────
DECLARE @subId INT;
DECLARE @owner INT;
DECLARE @n INT;
DECLARE @rev INT;

DECLARE sub_cur CURSOR LOCAL FAST_FORWARD FOR
    SELECT submission_id, student_id FROM dbo.submissions WHERE assignment_id = 1 ORDER BY submission_id;
OPEN sub_cur;
FETCH NEXT FROM sub_cur INTO @subId, @owner;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @n = 1;
    WHILE @n <= 3
    BEGIN
        SET @rev = ((@owner - 6 + @n) % 32) + 6;
        IF @rev = @owner
            SET @rev = ((@rev - 6 + 1) % 32) + 6;
        INSERT INTO dbo.peer_review_assignments (assignment_id, reviewer_id, submission_id, status)
        VALUES (1, @rev, @subId, 'pending');
        SET @n = @n + 1;
    END
    FETCH NEXT FROM sub_cur INTO @subId, @owner;
END
CLOSE sub_cur;
DEALLOCATE sub_cur;
GO

-- ── 11. 互评记录（24 份全完成；4 份部分完成；2 份全待评）──────
DECLARE @pra INT = 1;
DECLARE @maxPra INT = (SELECT MAX(pra_id) FROM dbo.peer_review_assignments WHERE assignment_id = 1);
DECLARE @subForPra INT;
DECLARE @ownerStu INT;
DECLARE @total DECIMAL(6,2);
DECLARE @revId INT;
DECLARE @slot INT;

WHILE @pra <= @maxPra
BEGIN
    SELECT @subForPra = submission_id FROM dbo.peer_review_assignments WHERE pra_id = @pra;
    SELECT @ownerStu = student_id FROM dbo.submissions WHERE submission_id = @subForPra;
    SELECT @slot = COUNT(*) FROM dbo.peer_review_assignments
        WHERE submission_id = @subForPra AND pra_id <= @pra;

    -- 最后 2 份提交（user 34/35）互评任务全部 pending
    IF @ownerStu >= 34
    BEGIN
        SET @pra = @pra + 1;
        CONTINUE;
    END

    -- 倒数第 3–6 份（user 30–33）：仅完成第 1 条互评，其余仍 pending
    IF @ownerStu >= 30 AND @slot > 1
    BEGIN
        SET @pra = @pra + 1;
        CONTINUE;
    END

    -- 低分样本：张晓明(stu6) 收到的互评偏低 → 可申诉
    IF @ownerStu = 6
        SET @total = CASE @slot WHEN 1 THEN 48.0 WHEN 2 THEN 52.0 ELSE 55.0 END;
    ELSE IF @ownerStu = 10 AND (@slot = 3)
        SET @total = 44.0;
    ELSE IF @ownerStu = 11 AND (@slot = 3)
        SET @total = 46.0;
    ELSE
        SET @total = 72.0 + (@pra % 17);

    -- 草稿互评：评阅人对 stu2026023(user 28) 的第 2 条派评保存草稿
    IF @ownerStu = 28 AND @slot = 2
    BEGIN
        INSERT INTO dbo.reviews (pra_id, overall_comment, total_score, status, submitted_at)
        VALUES (@pra, N'初稿：用例描述较完整，非功能需求待补充。', 68.0, 'draft', NULL);
        SET @revId = SCOPE_IDENTITY();
        INSERT INTO dbo.review_scores (review_id, rubric_item_id, score, comment) VALUES
        (@revId, 1, 17.0, N'草稿'), (@revId, 2, 10.0, N'草稿'),
        (@revId, 3, 14.0, N'草稿'), (@revId, 4, 10.0, N'草稿'), (@revId, 5, 17.0, N'草稿');
        SET @pra = @pra + 1;
        CONTINUE;
    END

    INSERT INTO dbo.reviews (pra_id, overall_comment, total_score, status, submitted_at)
    VALUES (@pra,
        CASE WHEN @total < 60 THEN N'文档有改进空间，部分章节偏简略。' ELSE N'整体完成度良好，结构清晰。' END,
        @total, 'submitted', DATEADD(DAY, -2 - (@pra % 4), GETDATE()));
    SET @revId = SCOPE_IDENTITY();

    INSERT INTO dbo.review_scores (review_id, rubric_item_id, score, comment) VALUES
    (@revId, 1, ROUND(@total * 0.25, 1), N'需求完整性评分'),
    (@revId, 2, ROUND(@total * 0.15, 1), N'非功能需求评分'),
    (@revId, 3, ROUND(@total * 0.20, 1), N'用例图评分'),
    (@revId, 4, ROUND(@total * 0.15, 1), N'排版评分'),
    (@revId, 5, ROUND(@total * 0.25, 1), N'创新性评分');

    UPDATE dbo.reviews SET total_score = (SELECT SUM(score) FROM dbo.review_scores WHERE review_id = @revId)
    WHERE review_id = @revId;

    UPDATE dbo.peer_review_assignments SET status = 'completed' WHERE pra_id = @pra;

    SET @pra = @pra + 1;
END;
GO

-- ── 12. 申诉（待处理 / 已通过 / 已驳回）──────────────────
INSERT INTO dbo.appeals (pra_id, submission_id, student_id, reason, status,
    handler_id, handler_response, adjusted_score, created_at, resolved_at)
SELECT TOP 1
    pra.pra_id, s.submission_id, s.student_id,
    N'互评分数偏低，我的 SRS 已覆盖借还书、检索、权限三个子系统，用例图已按反馈修改。',
    'pending', NULL, NULL, NULL, DATEADD(DAY, -1, GETDATE()), NULL
FROM dbo.peer_review_assignments pra
JOIN dbo.submissions s ON pra.submission_id = s.submission_id
WHERE s.assignment_id = 1 AND s.student_id = 6
ORDER BY pra.pra_id;

INSERT INTO dbo.appeals (pra_id, submission_id, student_id, reason, status,
    handler_id, handler_response, adjusted_score, created_at, resolved_at)
SELECT TOP 1
    pra.pra_id, s.submission_id, s.student_id,
    N'其中一份互评评论过于简略，未指出具体改进点，申请教师复核。',
    'accepted', 2,
    N'经复核，同意上调综合分。非功能需求章节已补充性能指标。',
    85.0, DATEADD(DAY, -6, GETDATE()), DATEADD(DAY, -3, GETDATE())
FROM dbo.peer_review_assignments pra
JOIN dbo.submissions s ON pra.submission_id = s.submission_id
WHERE s.assignment_id = 1 AND s.student_id = 6 AND pra.pra_id > 1
ORDER BY pra.pra_id;

INSERT INTO dbo.appeals (pra_id, submission_id, student_id, reason, status,
    handler_id, handler_response, adjusted_score, created_at, resolved_at)
SELECT TOP 1
    pra.pra_id, s.submission_id, s.student_id,
    N'认为互评同学打分过于严格，与量规描述不符。',
    'rejected', 2,
    N'评分在合理区间，评论已覆盖主要扣分点，申诉驳回。',
    NULL, DATEADD(DAY, -5, GETDATE()), DATEADD(DAY, -2, GETDATE())
FROM dbo.peer_review_assignments pra
JOIN dbo.submissions s ON pra.submission_id = s.submission_id
WHERE s.assignment_id = 1 AND s.student_id = 14
ORDER BY pra.pra_id;
GO

-- ── 13. 讨论区 ───────────────────────────────────────────
INSERT INTO dbo.discussions (assignment_id, student_id, content) VALUES
(1,  7,  N'用例图的 include 和 extend 有什么区别？我总是画混。'),
(1,  9,  N'include 是强制包含，extend 是可选扩展。我们实验报告里画过对比表。'),
(1,  8,  N'推荐 PlantUML，用代码画用例图版本管理很方便。'),
(1, 12, N'互评时看到有的同学非功能需求写得很细，学习了。'),
(1, 18, N'作业2的设计文档需要和作业1同一业务场景吗？'),
(1,  6, N'是的，课程要求同一项目贯穿需求→设计→测试。');
GO

-- ── 14. 通知 ─────────────────────────────────────────────
INSERT INTO dbo.notifications (user_id, title, content, is_read) VALUES
(6,  N'互评任务提醒',   N'你还有未完成的互评任务，请于 3/27 前完成。', 0),
(6,  N'申诉受理通知',   N'你的申诉已提交，等待教师处理。', 0),
(7,  N'作业发布通知',   N'第二次作业「系统设计文档」已发布。', 1),
(10, N'教师终审通知',   N'你的 SRS 作业已终审，成绩 92 分。', 1),
(14, N'申诉结果通知',   N'你的申诉经复核被驳回，详见申诉记录。', 1),
(4,  N'助教提醒',       N'软件工程课程有 1 条待处理申诉，请协助关注。', 0),
(2,  N'派评进度',       N'作业1互评完成率约 80%，作业2尚未派评。', 0),
(36, N'缺交提醒',       N'你尚未提交作业1，请尽快补交。', 0);
GO

-- ── 15. 汇总 ─────────────────────────────────────────────
PRINT N'';
PRINT N'========== 测试数据 v2 加载完成 ==========';
PRINT N'账号（密码均为 123456）：';
PRINT N'  admin01 | teacher01 | teacher02 | ta01 | stu2026001–032';
PRINT N'场景：';
PRINT N'  作业1 已派评+互评(24份完成/4份部分/2份待评) | 作业2 仅提交可派评 | 作业3 草稿';
PRINT N'  申诉: 1待处理(stu2026001) | 高相似: stu2026005/006 | 缺交: stu2026031/032 | 迟交: stu2026029/030';
PRINT N'==========================================';
GO
