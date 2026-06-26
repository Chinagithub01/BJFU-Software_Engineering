-- ============================================================
-- PeerReview 课程作业互评平台 · 数据库初始化脚本
-- 数据库: peerreview (PostgreSQL)
-- 选题编号: T-04
-- ============================================================

-- 清理 public schema（可重复执行）
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO CURRENT_USER;
GRANT ALL ON SCHEMA public TO public;

-- ============================================================
-- 1. 角色表
-- ============================================================
CREATE TABLE roles (
    role_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name   VARCHAR(20)  NOT NULL UNIQUE,
    description VARCHAR(100)
);

-- ============================================================
-- 2. 用户表
-- ============================================================
CREATE TABLE users (
    user_id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(50)   NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    real_name     VARCHAR(50)   NOT NULL,
    nickname      VARCHAR(50),
    email         VARCHAR(100),
    phone         VARCHAR(20),
    student_id    VARCHAR(30),
    school        VARCHAR(100),
    class_name    VARCHAR(100),
    role_id       INT           NOT NULL,
    avatar_url    VARCHAR(255),
    is_active     SMALLINT      DEFAULT 1,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- ============================================================
-- 3. 课程表
-- ============================================================
CREATE TABLE courses (
    course_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_name   VARCHAR(100) NOT NULL,
    course_code   VARCHAR(30)   NOT NULL UNIQUE,
    description   VARCHAR(500),
    teacher_id    INT           NOT NULL,
    semester      VARCHAR(20)  NOT NULL,
    is_archived   SMALLINT      DEFAULT 0,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id)
);

-- ============================================================
-- 4. 课程选课/助教指派表
-- ============================================================
CREATE TABLE course_enrollments (
    enrollment_id  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_id      INT          NOT NULL,
    user_id        INT          NOT NULL,
    role_in_course VARCHAR(20)  NOT NULL,
    enrolled_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT fk_enrollment_user   FOREIGN KEY (user_id)   REFERENCES users(user_id),
    CONSTRAINT uq_enrollment UNIQUE (course_id, user_id)
);

-- ============================================================
-- 5. 作业表
-- ============================================================
CREATE TABLE assignments (
    assignment_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_id         INT            NOT NULL,
    title             VARCHAR(200)  NOT NULL,
    description       TEXT,
    due_date          TIMESTAMP      NOT NULL,
    review_due_date   TIMESTAMP      NOT NULL,
    file_types        VARCHAR(100)   DEFAULT 'pdf,doc,docx,zip',
    max_file_size_mb  INT            DEFAULT 10,
    peer_review_count INT            DEFAULT 3,
    status            VARCHAR(20)    DEFAULT 'draft',
    created_by        INT            NOT NULL,
    created_at        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assignment_course  FOREIGN KEY (course_id) REFERENCES courses(course_id),
    CONSTRAINT fk_assignment_creator FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- ============================================================
-- 6. 评分量规(Rubric)表
-- ============================================================
CREATE TABLE rubrics (
    rubric_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    assignment_id INT NOT NULL UNIQUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rubric_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id)
);

-- ============================================================
-- 7. 量规评分项表
-- ============================================================
CREATE TABLE rubric_items (
    item_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rubric_id    INT            NOT NULL,
    item_name    VARCHAR(100)  NOT NULL,
    max_score    DECIMAL(5,1)   NOT NULL,
    weight       DECIMAL(3,2)   DEFAULT 1.0,
    description  VARCHAR(500),
    sort_order   INT            DEFAULT 0,
    CONSTRAINT fk_item_rubric FOREIGN KEY (rubric_id) REFERENCES rubrics(rubric_id)
);

-- ============================================================
-- 8. 提交记录表
-- ============================================================
CREATE TABLE submissions (
    submission_id  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    assignment_id  INT            NOT NULL,
    student_id     INT            NOT NULL,
    file_path      VARCHAR(500)   NOT NULL,
    file_name      VARCHAR(200)  NOT NULL,
    content_text   TEXT,
    content_hash   VARCHAR(64),
    similarity_pct DECIMAL(5,2)   DEFAULT 0,
    final_score    DECIMAL(6,2),
    final_comment  VARCHAR(500),
    status         VARCHAR(20)    DEFAULT 'submitted',
    submitted_at   TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sub_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id),
    CONSTRAINT fk_sub_student    FOREIGN KEY (student_id)   REFERENCES users(user_id),
    CONSTRAINT uq_submission UNIQUE (assignment_id, student_id)
);

-- ============================================================
-- 9. 互评派发表
-- ============================================================
CREATE TABLE peer_review_assignments (
    pra_id         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    assignment_id  INT       NOT NULL,
    reviewer_id    INT       NOT NULL,
    submission_id  INT       NOT NULL,
    status         VARCHAR(20) DEFAULT 'pending',
    assigned_at    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pra_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id),
    CONSTRAINT fk_pra_reviewer   FOREIGN KEY (reviewer_id)   REFERENCES users(user_id),
    CONSTRAINT fk_pra_submission FOREIGN KEY (submission_id) REFERENCES submissions(submission_id)
);

-- ============================================================
-- 10. 互评记录表
-- ============================================================
CREATE TABLE reviews (
    review_id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pra_id          INT            NOT NULL,
    overall_comment TEXT,
    total_score     DECIMAL(6,2),
    status          VARCHAR(20)    DEFAULT 'draft',
    submitted_at    TIMESTAMP,
    CONSTRAINT fk_review_pra FOREIGN KEY (pra_id) REFERENCES peer_review_assignments(pra_id),
    CONSTRAINT uq_review_per_pra UNIQUE (pra_id)
);

-- ============================================================
-- 11. 逐项评分表
-- ============================================================
CREATE TABLE review_scores (
    score_id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    review_id      INT            NOT NULL,
    rubric_item_id INT            NOT NULL,
    score          DECIMAL(5,1)   NOT NULL,
    comment        VARCHAR(500),
    CONSTRAINT fk_rs_review      FOREIGN KEY (review_id)      REFERENCES reviews(review_id),
    CONSTRAINT fk_rs_item        FOREIGN KEY (rubric_item_id) REFERENCES rubric_items(item_id),
    CONSTRAINT uq_score_per_item UNIQUE (review_id, rubric_item_id),
    CONSTRAINT ck_score_range    CHECK (score >= 0)
);

-- ============================================================
-- 12. 申诉表
-- ============================================================
CREATE TABLE appeals (
    appeal_id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pra_id           INT            NOT NULL,
    submission_id    INT            NOT NULL,
    student_id       INT            NOT NULL,
    reason           TEXT           NOT NULL,
    status           VARCHAR(20)    DEFAULT 'pending',
    handler_id       INT,
    handler_response TEXT,
    adjusted_score   DECIMAL(6,2),
    created_at       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    resolved_at      TIMESTAMP,
    CONSTRAINT fk_appeal_pra        FOREIGN KEY (pra_id)        REFERENCES peer_review_assignments(pra_id),
    CONSTRAINT fk_appeal_submission FOREIGN KEY (submission_id) REFERENCES submissions(submission_id),
    CONSTRAINT fk_appeal_student    FOREIGN KEY (student_id)    REFERENCES users(user_id),
    CONSTRAINT fk_appeal_handler    FOREIGN KEY (handler_id)    REFERENCES users(user_id)
);

-- ============================================================
-- 13. 匿名讨论表
-- ============================================================
CREATE TABLE discussions (
    discussion_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    assignment_id INT NOT NULL,
    student_id    INT NOT NULL,
    content       TEXT NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_disc_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id),
    CONSTRAINT fk_disc_student    FOREIGN KEY (student_id)    REFERENCES users(user_id)
);

-- ============================================================
-- 14. 通知表
-- ============================================================
CREATE TABLE notifications (
    notification_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         INT            NOT NULL,
    title           VARCHAR(200)  NOT NULL,
    content         TEXT,
    is_read         SMALLINT        DEFAULT 0,
    created_at      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ============================================================
-- 索引
-- ============================================================
CREATE INDEX idx_users_role      ON users(role_id);
CREATE INDEX idx_users_active    ON users(is_active);
CREATE INDEX idx_courses_teacher ON courses(teacher_id);
CREATE INDEX idx_courses_semester ON courses(semester);
CREATE INDEX idx_enroll_course   ON course_enrollments(course_id);
CREATE INDEX idx_enroll_user     ON course_enrollments(user_id);
CREATE INDEX idx_assign_course   ON assignments(course_id);
CREATE INDEX idx_assign_status   ON assignments(status);
CREATE INDEX idx_sub_assignment  ON submissions(assignment_id);
CREATE INDEX idx_sub_student     ON submissions(student_id);
CREATE INDEX idx_sub_hash        ON submissions(content_hash);
CREATE INDEX idx_pra_assignment  ON peer_review_assignments(assignment_id);
CREATE INDEX idx_pra_reviewer    ON peer_review_assignments(reviewer_id);
CREATE INDEX idx_pra_status      ON peer_review_assignments(status);
CREATE INDEX idx_review_status   ON reviews(status);
CREATE INDEX idx_appeal_status   ON appeals(status);
CREATE INDEX idx_appeal_student  ON appeals(student_id);
CREATE INDEX idx_notif_user      ON notifications(user_id, is_read);
