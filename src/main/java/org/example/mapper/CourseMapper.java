package org.example.mapper;

import org.example.entity.Course;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CourseMapper {
    int insertCourse(Course course);
    Course selectCourseById(Integer courseId);
    Course selectCourseByCode(@Param("courseCode") String courseCode);
    List<Course> selectAllCourses();
    // 新增：根据用户ID和角色身份查询所属课程
    List<Course> selectCoursesByUserId(@Param("userId") Integer userId, @Param("role") String role);
}
