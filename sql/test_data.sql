-- ============================================================
-- PeerReview · 测试数据 v2（PostgreSQL）
-- Docker 部署：由 init-db.sh 通过 psql 执行
-- ============================================================
-- 用户 ID 约定（顺序插入）：
--   1 admin | 2 teacher01 | 3 teacher02 | 4 ta01 | 5 ta02
--   6–37  stu2026001–stu2026032（32 人）
-- ============================================================

-- ── 1. 角色 ──────────────────────────────────────────────
INSERT INTO roles (role_name, description) VALUES
('admin',   '系统管理员'),
('teacher', '授课教师'),
('ta',      '课程助教'),
('student', '学生');

-- ── 2. 用户 ──────────────────────────────────────────────
INSERT INTO users (username, password_hash, real_name, nickname, email, student_id, class_name, role_id, is_active) VALUES
('admin',   '123456', '赵管理',   'Admin',   'admin01@bjfu.edu.cn',   NULL,       NULL,           1, 1);

INSERT INTO users (username, password_hash, real_name, nickname, email, student_id, class_name, role_id, is_active) VALUES
('teacher01', '123456', '李教授',   '李教授',   'liprof@bjfu.edu.cn',    NULL,       NULL,           2, 1),
('teacher02', '123456', '王副教授', '王副教授', 'wangap@bjfu.edu.cn',    NULL,       NULL,           2, 1);

INSERT INTO users (username, password_hash, real_name, nickname, email, student_id, class_name, role_id, is_active) VALUES
('ta01',      '123456', '陈助教',   '陈助教',   'chenta@bjfu.edu.cn',    NULL,       NULL,           3, 1),
('ta02',      '123456', '刘助教',   '刘助教',   'liuta@bjfu.edu.cn',     NULL,       NULL,           3, 1);

INSERT INTO users (username, password_hash, real_name, nickname, email, student_id, class_name, role_id, is_active) VALUES
('stu2026001','123456', '张晓明', '晓明', 'zhangxm@bjfu.edu.cn',  '2026001', '软工2301', 4, 1),
('stu2026002','123456', '李婷婷', '婷婷', 'litt@bjfu.edu.cn',     '2026002', '软工2301', 4, 1),
('stu2026003','123456', '王浩然', '浩然', 'wanghr@bjfu.edu.cn',   '2026003', '软工2301', 4, 1),
('stu2026004','123456', '赵思雨', '思雨', 'zhaosy@bjfu.edu.cn',   '2026004', '软工2301', 4, 1),
('stu2026005','123456', '刘子涵', '子涵', 'liuzh@bjfu.edu.cn',    '2026005', '软工2301', 4, 1),
('stu2026006','123456', '陈一鸣', '一鸣', 'chenym@bjfu.edu.cn',   '2026006', '软工2301', 4, 1),
('stu2026007','123456', '杨雨桐', '雨桐', 'yangyt@bjfu.edu.cn',   '2026007', '软工2301', 4, 1),
('stu2026008','123456', '周文博', '文博', 'zhouwb@bjfu.edu.cn',   '2026008', '软工2301', 4, 1),
('stu2026009','123456', '吴佳琪', '佳琪', 'wujq@bjfu.edu.cn',     '2026009', '软工2301', 4, 1),
('stu2026010','123456', '郑浩宇', '浩宇', 'zhenghy@bjfu.edu.cn',  '2026010', '软工2301', 4, 1),
('stu2026011','123456', '钱思远', '思远', 'qiansy@bjfu.edu.cn',   '2026011', '软工2301', 4, 1),
('stu2026012','123456', '孙雨萱', '雨萱', 'sunyx@bjfu.edu.cn',    '2026012', '软工2301', 4, 1),
('stu2026013','123456', '马俊杰', '俊杰', 'majj@bjfu.edu.cn',     '2026013', '软工2301', 4, 1),
('stu2026014','123456', '朱晓雯', '晓雯', 'zhuxw@bjfu.edu.cn',    '2026014', '软工2301', 4, 1),
('stu2026015','123456', '胡志强', '志强', 'huzq@bjfu.edu.cn',     '2026015', '软工2301', 4, 1),
('stu2026016','123456', '林诗涵', '诗涵', 'linsh@bjfu.edu.cn',    '2026016', '软工2301', 4, 1),
('stu2026017','123456', '何雨霏', '雨霏', 'heyf@bjfu.edu.cn',     '2026017', '软工2301', 4, 1),
('stu2026018','123456', '郭子轩', '子轩', 'guozx@bjfu.edu.cn',    '2026018', '软工2301', 4, 1),
('stu2026019','123456', '高思琪', '思琪', 'gaosq@bjfu.edu.cn',    '2026019', '软工2301', 4, 1),
('stu2026020','123456', '罗一航', '一航', 'luoyh@bjfu.edu.cn',    '2026020', '软工2301', 4, 1),
('stu2026021','123456', '梁婉清', '婉清', 'liangwq@bjfu.edu.cn',  '2026021', '软工2302', 4, 1),
('stu2026022','123456', '宋志远', '志远', 'songzy@bjfu.edu.cn',   '2026022', '软工2302', 4, 1),
('stu2026023','123456', '唐雨薇', '雨薇', 'tangyw@bjfu.edu.cn',   '2026023', '软工2302', 4, 1),
('stu2026024','123456', '韩明哲', '明哲', 'hanmz@bjfu.edu.cn',    '2026024', '软工2302', 4, 1),
('stu2026025','123456', '冯佳琳', '佳琳', 'fengjl@bjfu.edu.cn',   '2026025', '软工2302', 4, 1),
('stu2026026','123456', '董子涵', '子涵', 'dongzh@bjfu.edu.cn',   '2026026', '软工2302', 4, 1),
('stu2026027','123456', '袁博文', '博文', 'yuanbw@bjfu.edu.cn',   '2026027', '软工2302', 4, 1),
('stu2026028','123456', '邓雨晴', '雨晴', 'dengyq@bjfu.edu.cn',   '2026028', '软工2302', 4, 1),
('stu2026029','123456', '许浩宸', '浩宸', 'xuhc@bjfu.edu.cn',     '2026029', '软工2302', 4, 1),
('stu2026030','123456', '傅思源', '思源', 'fusy@bjfu.edu.cn',     '2026030', '软工2302', 4, 1),
('stu2026031','123456', '沈乐天', '乐天', 'shenlt@bjfu.edu.cn',   '2026031', '软工2302', 4, 1),
('stu2026032','123456', '曾雨彤', '雨彤', 'zengyt@bjfu.edu.cn',   '2026032', '软工2302', 4, 1);

INSERT INTO users (username, password_hash, real_name, email, role_id, is_active) VALUES
('locked_stu', '123456', '已停用用户', 'locked@bjfu.edu.cn', 4, 0);

-- ── 3. 课程 ──────────────────────────────────────────────
INSERT INTO courses (course_name, course_code, description, teacher_id, semester, is_archived) VALUES
('软件工程', 'CS310-01',
 '专业核心课：需求分析、系统设计、编码测试与项目管理。主演示课程。',
 2, '2026春', 0),
('数据结构', 'CS210-01',
 '算法与数据结构基础。用于演示多教师、多课程隔离。',
 3, '2026春', 0);

-- ── 4. 选课 ──────────────────────────────────────────────
DO $$
DECLARE uid INT;
BEGIN
    FOR uid IN 6..37 LOOP
        INSERT INTO course_enrollments (course_id, user_id, role_in_course) VALUES (1, uid, 'student');
    END LOOP;

    INSERT INTO course_enrollments (course_id, user_id, role_in_course) VALUES
    (1, 4, 'ta'), (1, 5, 'ta');

    FOR uid IN 6..17 LOOP
        INSERT INTO course_enrollments (course_id, user_id, role_in_course) VALUES (2, uid, 'student');
    END LOOP;
END $$;

-- ── 5. 作业 ──────────────────────────────────────────────
INSERT INTO assignments (course_id, title, description, due_date, review_due_date,
    file_types, max_file_size_mb, peer_review_count, status, created_by) VALUES
(1, '第一次作业：需求分析文档（SRS）',
 '选择真实场景编写 SRS，含功能/非功能需求、用例图与类图初稿。',
 '2026-03-20 23:59:59', '2026-03-27 23:59:59', 'pdf,doc,docx', 20, 3, 'published', 2),

(1, '第二次作业：系统设计文档',
 '基于作业1场景完成概要设计与详细设计，含架构图、ER 图、核心类图。',
 '2026-05-10 23:59:59', '2026-05-17 23:59:59', 'pdf,doc,docx,zip', 50, 3, 'published', 2),

(1, '第三次作业：测试计划（草稿）',
 '撰写系统测试计划与用例表。当前为草稿，用于演示发布流程。',
 '2026-06-01 23:59:59', '2026-06-08 23:59:59', 'pdf,docx', 10, 2, 'draft', 2),

(2, '数据结构作业1：线性表应用',
 '实现并分析顺序表/链表的插入删除，提交实验报告。',
 '2026-04-15 23:59:59', '2026-04-22 23:59:59', 'pdf', 10, 2, 'published', 3);

-- ── 6. 量规 ──────────────────────────────────────────────
INSERT INTO rubrics (assignment_id) VALUES (1), (2), (4);

INSERT INTO rubric_items (rubric_id, item_name, max_score, weight, description, sort_order) VALUES
(1, '需求完整性',       25, 1.0, '功能需求覆盖是否完整',           1),
(1, '非功能需求',       15, 1.0, '性能、安全、可用性描述',         2),
(1, '用例图规范性',     20, 1.0, 'UML 用例图是否符合规范',         3),
(1, '文档结构与排版',   15, 1.0, '章节结构与排版',                 4),
(1, '创新性与分析深度', 25, 1.0, '业务分析深度与创新点',           5);

INSERT INTO rubric_items (rubric_id, item_name, max_score, weight, description, sort_order) VALUES
(2, '体系结构合理性',   20, 1.0, '分层与架构风格',                 1),
(2, '模块划分与接口',   20, 1.0, '模块粒度与接口定义',             2),
(2, '数据库设计',       25, 1.0, 'ER 图与表结构设计',              3),
(2, '类图与详细设计',   20, 1.0, '核心类图与设计模式',             4),
(2, '文档质量与完整性', 15, 1.0, '文档完整性',                     5);

INSERT INTO rubric_items (rubric_id, item_name, max_score, weight, description, sort_order) VALUES
(3, '算法正确性',       40, 1.0, '线性表操作实现正确',             1),
(3, '复杂度分析',       30, 1.0, '时间与空间复杂度分析',           2),
(3, '实验报告质量',     30, 1.0, '实验步骤与结果说明',             3);

-- ── 7. 作业1 提交 ────────────────────────────────────────
DO $$
DECLARE
    stu INT;
    seq INT := 0;
    hash_val VARCHAR(64);
    sim DECIMAL(5,2);
    txt TEXT;
    fname VARCHAR(200);
    late_status VARCHAR(20);
BEGIN
    stu := 6;
    WHILE stu <= 35 LOOP
        seq := seq + 1;
        hash_val := md5('srs-a1-' || stu::text);
        sim := 5.0 + (seq % 8) * 2.5;
        txt := '【SRS】学号' || (stu - 5)::text || '：图书馆管理系统需求分析，含借还书、检索、权限管理用例。';
        fname := 'SRS_学号' || lpad((stu - 5)::text, 2, '0') || '.pdf';
        late_status := CASE WHEN stu IN (34, 35) THEN 'late' ELSE 'submitted' END;

        IF stu IN (10, 11) THEN
            hash_val := 'a1b2c3d4e5f6789012345678abcdef01';
            sim := CASE WHEN stu = 15 THEN 92.5 ELSE 88.0 END;
            txt := '【SRS】图书馆管理系统：高度相似的模板化需求正文，用于 simhash 查重演示。';
        END IF;

        INSERT INTO submissions (assignment_id, student_id, file_path, file_name, content_text,
            content_hash, similarity_pct, status, submitted_at)
        VALUES (1, stu,
            'uploads/demo/a1/stu' || stu::text || '.pdf',
            fname, txt, hash_val, sim, late_status,
            TIMESTAMP '2026-03-20 20:00:00' - ((seq % 36) || ' hours')::INTERVAL);

        stu := stu + 1;
    END LOOP;
END $$;

UPDATE submissions SET final_score = 92.0, final_comment = '需求完整、用例清晰，终审优秀。'
WHERE assignment_id = 1 AND student_id = 10;
UPDATE submissions SET final_score = 88.5, final_comment = '结构规范，非功能需求可再细化。'
WHERE assignment_id = 1 AND student_id = 12;
UPDATE submissions SET final_score = 85.0, final_comment = '申诉复核后确定的最终成绩。'
WHERE assignment_id = 1 AND student_id = 6;

-- ── 8. 作业2 提交 ────────────────────────────────────────
DO $$
DECLARE
    stu2 INT;
    seq2 INT := 0;
BEGIN
    stu2 := 6;
    WHILE stu2 <= 23 LOOP
        seq2 := seq2 + 1;
        INSERT INTO submissions (assignment_id, student_id, file_path, file_name, content_text,
            content_hash, similarity_pct, status, submitted_at)
        VALUES (2, stu2,
            'uploads/demo/a2/stu' || stu2::text || '.pdf',
            '设计文档_学号' || lpad((stu2 - 5)::text, 2, '0') || '.pdf',
            '【设计】学号' || (stu2 - 5)::text || '：系统概要设计、模块划分与数据库 ER 图。',
            md5('design-a2-' || stu2::text),
            3.0, 'submitted',
            TIMESTAMP '2026-07-10 18:00:00' - ((seq2 % 24) || ' hours')::INTERVAL);
        stu2 := stu2 + 1;
    END LOOP;
END $$;

-- ── 9. 数据结构课 提交 ───────────────────────────────────
DO $$
DECLARE
    stu3 INT;
    seq3 INT := 0;
BEGIN
    stu3 := 6;
    WHILE stu3 <= 13 LOOP
        seq3 := seq3 + 1;
        INSERT INTO submissions (assignment_id, student_id, file_path, file_name, content_text,
            content_hash, similarity_pct, status, submitted_at)
        VALUES (4, stu3,
            'uploads/demo/ds/stu' || stu3::text || '.pdf',
            '线性表实验_学号' || lpad((stu3 - 5)::text, 2, '0') || '.pdf',
            '顺序表与链表插入删除实验及复杂度分析。',
            md5('ds-a4-' || stu3::text),
            0, 'submitted',
            TIMESTAMP '2026-07-15 12:00:00' - (seq3 || ' hours')::INTERVAL);
        stu3 := stu3 + 1;
    END LOOP;
END $$;

-- ── 10. 作业1 派评 ───────────────────────────────────────
DO $$
DECLARE
    rec RECORD;
    n INT;
    rev INT;
    owner INT;
BEGIN
    FOR rec IN SELECT submission_id, student_id FROM submissions WHERE assignment_id = 1 ORDER BY submission_id LOOP
        owner := rec.student_id;
        n := 1;
        WHILE n <= 3 LOOP
            rev := ((owner - 6 + n) % 32) + 6;
            IF rev = owner THEN
                rev := ((rev - 6 + 1) % 32) + 6;
            END IF;
            INSERT INTO peer_review_assignments (assignment_id, reviewer_id, submission_id, status)
            VALUES (1, rev, rec.submission_id, 'pending');
            n := n + 1;
        END LOOP;
    END LOOP;
END $$;

-- ── 11. 互评记录 ─────────────────────────────────────────
DO $$
DECLARE
    pra INT := 1;
    max_pra INT;
    sub_for_pra INT;
    owner_stu INT;
    total DECIMAL(6,2);
    rev_id INT;
    slot INT;
BEGIN
    SELECT COALESCE(MAX(pra_id), 0) INTO max_pra FROM peer_review_assignments WHERE assignment_id = 1;

    WHILE pra <= max_pra LOOP
        SELECT submission_id INTO sub_for_pra FROM peer_review_assignments WHERE pra_id = pra;
        SELECT student_id INTO owner_stu FROM submissions WHERE submission_id = sub_for_pra;
        SELECT COUNT(*) INTO slot FROM peer_review_assignments
            WHERE submission_id = sub_for_pra AND pra_id <= pra;

        IF owner_stu >= 34 THEN
            pra := pra + 1;
            CONTINUE;
        END IF;

        IF owner_stu >= 30 AND slot > 1 THEN
            pra := pra + 1;
            CONTINUE;
        END IF;

        IF owner_stu = 6 THEN
            total := CASE slot WHEN 1 THEN 48.0 WHEN 2 THEN 52.0 ELSE 55.0 END;
        ELSIF owner_stu = 10 AND slot = 3 THEN
            total := 44.0;
        ELSIF owner_stu = 11 AND slot = 3 THEN
            total := 46.0;
        ELSE
            total := 72.0 + (pra % 17);
        END IF;

        IF owner_stu = 28 AND slot = 2 THEN
            INSERT INTO reviews (pra_id, overall_comment, total_score, status, submitted_at)
            VALUES (pra, '初稿：用例描述较完整，非功能需求待补充。', 68.0, 'draft', NULL)
            RETURNING review_id INTO rev_id;

            INSERT INTO review_scores (review_id, rubric_item_id, score, comment) VALUES
            (rev_id, 1, 17.0, '草稿'), (rev_id, 2, 10.0, '草稿'),
            (rev_id, 3, 14.0, '草稿'), (rev_id, 4, 10.0, '草稿'), (rev_id, 5, 17.0, '草稿');

            pra := pra + 1;
            CONTINUE;
        END IF;

        INSERT INTO reviews (pra_id, overall_comment, total_score, status, submitted_at)
        VALUES (pra,
            CASE WHEN total < 60 THEN '文档有改进空间，部分章节偏简略。' ELSE '整体完成度良好，结构清晰。' END,
            total, 'submitted', CURRENT_TIMESTAMP - ((2 + (pra % 4)) || ' days')::INTERVAL)
        RETURNING review_id INTO rev_id;

        INSERT INTO review_scores (review_id, rubric_item_id, score, comment) VALUES
        (rev_id, 1, ROUND((total * 0.25)::numeric, 1), '需求完整性评分'),
        (rev_id, 2, ROUND((total * 0.15)::numeric, 1), '非功能需求评分'),
        (rev_id, 3, ROUND((total * 0.20)::numeric, 1), '用例图评分'),
        (rev_id, 4, ROUND((total * 0.15)::numeric, 1), '排版评分'),
        (rev_id, 5, ROUND((total * 0.25)::numeric, 1), '创新性评分');

        UPDATE reviews SET total_score = (SELECT SUM(score) FROM review_scores WHERE review_id = rev_id)
        WHERE review_id = rev_id;

        UPDATE peer_review_assignments SET status = 'completed' WHERE pra_id = pra;

        pra := pra + 1;
    END LOOP;
END $$;

-- ── 12. 申诉 ─────────────────────────────────────────────
INSERT INTO appeals (pra_id, submission_id, student_id, reason, status,
    handler_id, handler_response, adjusted_score, created_at, resolved_at)
SELECT
    pra.pra_id, s.submission_id, s.student_id,
    '互评分数偏低，我的 SRS 已覆盖借还书、检索、权限三个子系统，用例图已按反馈修改。',
    'pending', NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '1 day', NULL
FROM peer_review_assignments pra
JOIN submissions s ON pra.submission_id = s.submission_id
WHERE s.assignment_id = 1 AND s.student_id = 6
ORDER BY pra.pra_id
LIMIT 1;

INSERT INTO appeals (pra_id, submission_id, student_id, reason, status,
    handler_id, handler_response, adjusted_score, created_at, resolved_at)
SELECT
    pra.pra_id, s.submission_id, s.student_id,
    '其中一份互评评论过于简略，未指出具体改进点，申请教师复核。',
    'accepted', 2,
    '经复核，同意上调综合分。非功能需求章节已补充性能指标。',
    85.0, CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '3 days'
FROM peer_review_assignments pra
JOIN submissions s ON pra.submission_id = s.submission_id
WHERE s.assignment_id = 1 AND s.student_id = 6 AND pra.pra_id > 1
ORDER BY pra.pra_id
LIMIT 1;

INSERT INTO appeals (pra_id, submission_id, student_id, reason, status,
    handler_id, handler_response, adjusted_score, created_at, resolved_at)
SELECT
    pra.pra_id, s.submission_id, s.student_id,
    '认为互评同学打分过于严格，与量规描述不符。',
    'rejected', 2,
    '评分在合理区间，评论已覆盖主要扣分点，申诉驳回。',
    NULL, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
FROM peer_review_assignments pra
JOIN submissions s ON pra.submission_id = s.submission_id
WHERE s.assignment_id = 1 AND s.student_id = 14
ORDER BY pra.pra_id
LIMIT 1;

-- ── 13. 讨论区 ───────────────────────────────────────────
INSERT INTO discussions (assignment_id, student_id, content) VALUES
(1,  7,  '用例图的 include 和 extend 有什么区别？我总是画混。'),
(1,  9,  'include 是强制包含，extend 是可选扩展。我们实验报告里画过对比表。'),
(1,  8,  '推荐 PlantUML，用代码画用例图版本管理很方便。'),
(1, 12, '互评时看到有的同学非功能需求写得很细，学习了。'),
(1, 18, '作业2的设计文档需要和作业1同一业务场景吗？'),
(1,  6, '是的，课程要求同一项目贯穿需求→设计→测试。');

-- ── 14. 通知 ─────────────────────────────────────────────
INSERT INTO notifications (user_id, title, content, is_read) VALUES
(6,  '互评任务提醒',   '你还有未完成的互评任务，请于 3/27 前完成。', 0),
(6,  '申诉受理通知',   '你的申诉已提交，等待教师处理。', 0),
(7,  '作业发布通知',   '第二次作业「系统设计文档」已发布。', 1),
(10, '教师终审通知',   '你的 SRS 作业已终审，成绩 92 分。', 1),
(14, '申诉结果通知',   '你的申诉经复核被驳回，详见申诉记录。', 1),
(4,  '助教提醒',       '软件工程课程有 1 条待处理申诉，请协助关注。', 0),
(2,  '派评进度',       '作业1互评完成率约 80%，作业2尚未派评。', 0),
(36, '缺交提醒',       '你尚未提交作业1，请尽快补交。', 0);
