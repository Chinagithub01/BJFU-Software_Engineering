-- ============================================================
-- PeerReview 课程作业互评平台 · 数据库初始化脚本
-- 数据库: softWare (SQL Server)
-- 选题编号: T-04
-- 基于: 00_软件工程课程设计任务书_2026春 & T-04_PeerReview选题指南
-- ============================================================

-- 创建数据库（如尚未创建）
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'softWare')
BEGIN
    CREATE DATABASE softWare;
END
GO

USE softWare;
GO

-- ============================================================
-- 清理：先删所有外键，再删所有表（可重复执行）
-- ============================================================
DECLARE @sql NVARCHAR(MAX) = '';

-- 删掉所有外键约束
SELECT @sql = @sql + 'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id))
    + '.' + QUOTENAME(OBJECT_NAME(parent_object_id))
    + ' DROP CONSTRAINT ' + QUOTENAME(name) + ';'
FROM sys.foreign_keys;

IF @sql <> '' EXEC sp_executesql @sql;

-- 删掉所有用户表
SET @sql = '';
SELECT @sql = @sql + 'DROP TABLE IF EXISTS ' + QUOTENAME(OBJECT_SCHEMA_NAME(object_id))
    + '.' + QUOTENAME(name) + ';'
FROM sys.tables;

IF @sql <> '' EXEC sp_executesql @sql;
GO

-- ============================================================
-- 1. 角色表
-- ============================================================
CREATE TABLE dbo.roles (
    role_id     INT IDENTITY(1,1) PRIMARY KEY,
    role_name   VARCHAR(20)  NOT NULL UNIQUE,   -- admin / teacher / ta / student
    description NVARCHAR(100)
);
GO

-- ============================================================
-- 2. 用户表
-- ============================================================
CREATE TABLE dbo.users (
    user_id       INT IDENTITY(1,1) PRIMARY KEY,
    username      VARCHAR(50)   NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    real_name     NVARCHAR(50)  NOT NULL,
    nickname      NVARCHAR(50),
    email         VARCHAR(100),
    phone         VARCHAR(20),
    student_id    VARCHAR(30),
    school        NVARCHAR(100),
    class_name    NVARCHAR(100),
    role_id       INT           NOT NULL,
    avatar_url    VARCHAR(255),
    is_active     TINYINT       DEFAULT 1,
    created_at    DATETIME2     DEFAULT GETDATE(),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES dbo.roles(role_id)
);
GO

-- ============================================================
-- 3. 课程表
-- ============================================================
CREATE TABLE dbo.courses (
    course_id     INT IDENTITY(1,1) PRIMARY KEY,
    course_name   NVARCHAR(100) NOT NULL,
    course_code   VARCHAR(30)   NOT NULL UNIQUE,
    description   NVARCHAR(500),
    teacher_id    INT           NOT NULL,
    semester      NVARCHAR(20)  NOT NULL,
    is_archived   TINYINT       DEFAULT 0,
    created_at    DATETIME2     DEFAULT GETDATE(),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES dbo.users(user_id)
);
GO

-- ============================================================
-- 4. 课程选课/助教指派表
-- ============================================================
CREATE TABLE dbo.course_enrollments (
    enrollment_id  INT IDENTITY(1,1) PRIMARY KEY,
    course_id      INT          NOT NULL,
    user_id        INT          NOT NULL,
    role_in_course VARCHAR(20)  NOT NULL,   -- student / ta
    enrolled_at    DATETIME2    DEFAULT GETDATE(),
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES dbo.courses(course_id),
    CONSTRAINT fk_enrollment_user   FOREIGN KEY (user_id)   REFERENCES dbo.users(user_id),
    CONSTRAINT uq_enrollment UNIQUE (course_id, user_id)
);
GO

-- ============================================================
-- 5. 作业表
-- ============================================================
CREATE TABLE dbo.assignments (
    assignment_id     INT IDENTITY(1,1) PRIMARY KEY,
    course_id         INT            NOT NULL,
    title             NVARCHAR(200)  NOT NULL,
    description       NVARCHAR(MAX),
    due_date          DATETIME2      NOT NULL,
    review_due_date   DATETIME2      NOT NULL,
    file_types        VARCHAR(100)   DEFAULT 'pdf,doc,docx,zip',
    max_file_size_mb  INT            DEFAULT 10,
    peer_review_count INT            DEFAULT 3,
    status            VARCHAR(20)    DEFAULT 'draft',
    created_by        INT            NOT NULL,
    created_at        DATETIME2      DEFAULT GETDATE(),
    CONSTRAINT fk_assignment_course  FOREIGN KEY (course_id) REFERENCES dbo.courses(course_id),
    CONSTRAINT fk_assignment_creator FOREIGN KEY (created_by) REFERENCES dbo.users(user_id)
);
GO

-- ============================================================
-- 6. 评分量规(Rubric)表
-- ============================================================
CREATE TABLE dbo.rubrics (
    rubric_id     INT IDENTITY(1,1) PRIMARY KEY,
    assignment_id INT NOT NULL UNIQUE,
    created_at    DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_rubric_assignment FOREIGN KEY (assignment_id) REFERENCES dbo.assignments(assignment_id)
);
GO

-- ============================================================
-- 7. 量规评分项表
-- ============================================================
CREATE TABLE dbo.rubric_items (
    item_id      INT IDENTITY(1,1) PRIMARY KEY,
    rubric_id    INT            NOT NULL,
    item_name    NVARCHAR(100)  NOT NULL,
    max_score    DECIMAL(5,1)   NOT NULL,
    weight       DECIMAL(3,2)   DEFAULT 1.0,
    description  NVARCHAR(500),
    sort_order   INT            DEFAULT 0,
    CONSTRAINT fk_item_rubric FOREIGN KEY (rubric_id) REFERENCES dbo.rubrics(rubric_id)
);
GO

-- ============================================================
-- 8. 提交记录表
-- ============================================================
CREATE TABLE dbo.submissions (
    submission_id  INT IDENTITY(1,1) PRIMARY KEY,
    assignment_id  INT            NOT NULL,
    student_id     INT            NOT NULL,
    file_path      VARCHAR(500)   NOT NULL,
    file_name      NVARCHAR(200)  NOT NULL,
    content_text   NVARCHAR(MAX),
    content_hash   VARCHAR(64),
    similarity_pct DECIMAL(5,2)   DEFAULT 0,
    final_score    DECIMAL(6,2),
    final_comment  NVARCHAR(500),
    status         VARCHAR(20)    DEFAULT 'submitted',
    submitted_at   DATETIME2      DEFAULT GETDATE(),
    CONSTRAINT fk_sub_assignment FOREIGN KEY (assignment_id) REFERENCES dbo.assignments(assignment_id),
    CONSTRAINT fk_sub_student    FOREIGN KEY (student_id)   REFERENCES dbo.users(user_id),
    CONSTRAINT uq_submission UNIQUE (assignment_id, student_id)
);
GO

-- ============================================================
-- 9. 互评派发表（防自评逻辑在应用层实现）
-- ============================================================
CREATE TABLE dbo.peer_review_assignments (
    pra_id         INT IDENTITY(1,1) PRIMARY KEY,
    assignment_id  INT       NOT NULL,
    reviewer_id    INT       NOT NULL,
    submission_id  INT       NOT NULL,
    status         VARCHAR(20) DEFAULT 'pending',
    assigned_at    DATETIME2  DEFAULT GETDATE(),
    CONSTRAINT fk_pra_assignment FOREIGN KEY (assignment_id) REFERENCES dbo.assignments(assignment_id),
    CONSTRAINT fk_pra_reviewer   FOREIGN KEY (reviewer_id)   REFERENCES dbo.users(user_id),
    CONSTRAINT fk_pra_submission FOREIGN KEY (submission_id) REFERENCES dbo.submissions(submission_id)
);
GO

-- ============================================================
-- 10. 互评记录表
-- ============================================================
CREATE TABLE dbo.reviews (
    review_id       INT IDENTITY(1,1) PRIMARY KEY,
    pra_id          INT            NOT NULL,
    overall_comment NVARCHAR(MAX),
    total_score     DECIMAL(6,2),
    status          VARCHAR(20)    DEFAULT 'draft',
    submitted_at    DATETIME2,
    CONSTRAINT fk_review_pra FOREIGN KEY (pra_id) REFERENCES dbo.peer_review_assignments(pra_id),
    CONSTRAINT uq_review_per_pra UNIQUE (pra_id)
);
GO

-- ============================================================
-- 11. 逐项评分表
-- ============================================================
CREATE TABLE dbo.review_scores (
    score_id       INT IDENTITY(1,1) PRIMARY KEY,
    review_id      INT            NOT NULL,
    rubric_item_id INT            NOT NULL,
    score          DECIMAL(5,1)   NOT NULL,
    comment        NVARCHAR(500),
    CONSTRAINT fk_rs_review      FOREIGN KEY (review_id)      REFERENCES dbo.reviews(review_id),
    CONSTRAINT fk_rs_item        FOREIGN KEY (rubric_item_id) REFERENCES dbo.rubric_items(item_id),
    CONSTRAINT uq_score_per_item UNIQUE (review_id, rubric_item_id),
    CONSTRAINT ck_score_range    CHECK (score >= 0)
);
GO

-- ============================================================
-- 12. 申诉表
-- ============================================================
CREATE TABLE dbo.appeals (
    appeal_id        INT IDENTITY(1,1) PRIMARY KEY,
    pra_id           INT            NOT NULL,
    submission_id    INT            NOT NULL,
    student_id       INT            NOT NULL,
    reason           NVARCHAR(MAX)  NOT NULL,
    status           VARCHAR(20)    DEFAULT 'pending',
    handler_id       INT,
    handler_response NVARCHAR(MAX),
    adjusted_score   DECIMAL(6,2),
    created_at       DATETIME2      DEFAULT GETDATE(),
    resolved_at      DATETIME2,
    CONSTRAINT fk_appeal_pra        FOREIGN KEY (pra_id)        REFERENCES dbo.peer_review_assignments(pra_id),
    CONSTRAINT fk_appeal_submission FOREIGN KEY (submission_id) REFERENCES dbo.submissions(submission_id),
    CONSTRAINT fk_appeal_student    FOREIGN KEY (student_id)    REFERENCES dbo.users(user_id),
    CONSTRAINT fk_appeal_handler    FOREIGN KEY (handler_id)    REFERENCES dbo.users(user_id)
);
GO

-- ============================================================
-- 13. 匿名讨论表
CREATE TABLE dbo.discussions (
    discussion_id INT IDENTITY(1,1) PRIMARY KEY,
    assignment_id INT NOT NULL,
    student_id    INT NOT NULL,
    content       NVARCHAR(MAX) NOT NULL,
    created_at    DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT fk_disc_assignment FOREIGN KEY (assignment_id) REFERENCES dbo.assignments(assignment_id),
    CONSTRAINT fk_disc_student    FOREIGN KEY (student_id)    REFERENCES dbo.users(user_id)
);
GO

-- 14. 通知表
-- ============================================================
CREATE TABLE dbo.notifications (
    notification_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id         INT            NOT NULL,
    title           NVARCHAR(200)  NOT NULL,
    content         NVARCHAR(MAX),
    is_read         TINYINT        DEFAULT 0,
    created_at      DATETIME2      DEFAULT GETDATE(),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
);
GO

-- ============================================================
-- 索引
-- ============================================================
CREATE INDEX idx_users_role      ON dbo.users(role_id);
CREATE INDEX idx_users_active    ON dbo.users(is_active);
CREATE INDEX idx_courses_teacher ON dbo.courses(teacher_id);
CREATE INDEX idx_courses_semester ON dbo.courses(semester);
CREATE INDEX idx_enroll_course   ON dbo.course_enrollments(course_id);
CREATE INDEX idx_enroll_user     ON dbo.course_enrollments(user_id);
CREATE INDEX idx_assign_course   ON dbo.assignments(course_id);
CREATE INDEX idx_assign_status   ON dbo.assignments(status);
CREATE INDEX idx_sub_assignment  ON dbo.submissions(assignment_id);
CREATE INDEX idx_sub_student     ON dbo.submissions(student_id);
CREATE INDEX idx_sub_hash        ON dbo.submissions(content_hash);
CREATE INDEX idx_pra_assignment  ON dbo.peer_review_assignments(assignment_id);
CREATE INDEX idx_pra_reviewer    ON dbo.peer_review_assignments(reviewer_id);
CREATE INDEX idx_pra_status      ON dbo.peer_review_assignments(status);
CREATE INDEX idx_review_status   ON dbo.reviews(status);
CREATE INDEX idx_appeal_status   ON dbo.appeals(status);
CREATE INDEX idx_appeal_student  ON dbo.appeals(student_id);
CREATE INDEX idx_notif_user      ON dbo.notifications(user_id, is_read);
GO

PRINT '>> 所有表结构创建完成 (14 张表)';
GO
