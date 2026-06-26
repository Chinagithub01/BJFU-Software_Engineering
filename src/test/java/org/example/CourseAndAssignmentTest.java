package org.example;

import org.apache.ibatis.session.SqlSession;
import org.example.entity.Assignment;
import org.example.entity.Course;
import org.example.mapper.AssignmentMapper;
import org.example.mapper.CourseMapper;
import org.example.util.MyBatisUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CourseAndAssignmentTest {

    private SqlSession sqlSession;
    private CourseMapper courseMapper;
    private AssignmentMapper assignmentMapper;

    @Before
    public void setUp() {
        // 打开一个新的 SqlSession
        sqlSession = MyBatisUtil.getSqlSession();
        // 获取 Mapper 代理对象
        courseMapper = sqlSession.getMapper(CourseMapper.class);
        assignmentMapper = sqlSession.getMapper(AssignmentMapper.class);
    }

    @After
    public void tearDown() {
        if (sqlSession != null) {
            // 测试完成后回滚事务（避免测试脏数据污染数据库），或者根据需要 commit
            sqlSession.rollback(); 
            sqlSession.close();
        }
    }

    @Test
    public void testInsertAndSelectCourse() {
        // 1. 创建 Course 实体
        Course course = new Course();
        course.setCourseName("软件工程(课程设计)");
        course.setCourseCode("SE-" + System.currentTimeMillis()); // 保证唯一
        course.setDescription("测试课程描述");
        course.setTeacherId(1); // 假设数据库中有一个 teacher_id=1 的用户
        course.setSemester("2026春");
        course.setIsArchived(0);

        // 2. 插入 Course
        int rows = courseMapper.insertCourse(course);
        assertTrue(rows > 0);
        assertNotNull(course.getCourseId()); // 验证自增主键是否返回
        
        System.out.println("插入的课程ID: " + course.getCourseId());

        // 3. 查询刚刚插入的 Course
        Course retrievedCourse = courseMapper.selectCourseById(course.getCourseId());
        assertNotNull(retrievedCourse);
        System.out.println("查询到的课程: " + retrievedCourse);
    }

    @Test
    public void testInsertAndSelectAssignment() {
        // 首先需要有一个可用的 Course
        Course course = new Course();
        course.setCourseName("测试作业所在课程");
        course.setCourseCode("CS-" + System.currentTimeMillis());
        course.setTeacherId(1);
        course.setSemester("2026春");
        course.setIsArchived(0);
        courseMapper.insertCourse(course);
        
        Integer courseId = course.getCourseId();

        // 1. 创建 Assignment 实体
        Assignment assignment = new Assignment();
        assignment.setCourseId(courseId);
        assignment.setTitle("M1 模块提交作业");
        assignment.setDescription("请提交你的课程设计报告");
        
        // 设置日期：假设截止日期是明天，互评截止日期是后天
        long now = System.currentTimeMillis();
        assignment.setDueDate(new Date(now + 86400000L));
        assignment.setReviewDueDate(new Date(now + 2 * 86400000L));
        
        assignment.setFileTypes("pdf,doc,docx");
        assignment.setMaxFileSizeMb(10);
        assignment.setPeerReviewCount(3);
        assignment.setStatus("published");
        assignment.setCreatedBy(1);

        // 2. 插入 Assignment
        int rows = assignmentMapper.insertAssignment(assignment);
        assertTrue(rows > 0);
        assertNotNull(assignment.getAssignmentId());
        
        System.out.println("插入的作业ID: " + assignment.getAssignmentId());

        // 3. 根据 CourseId 查询作业列表
        List<Assignment> assignments = assignmentMapper.selectAssignmentsByCourseId(courseId);
        assertTrue(assignments.size() > 0);
        System.out.println("该课程下的作业列表: " + assignments);
    }
}