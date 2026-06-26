package org.example.mapper;

import org.example.entity.CourseMemberDTO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface EnrollmentMapper {
    List<CourseMemberDTO> selectMembersByCourseId(Integer courseId);
    int insertEnrollment(@Param("courseId") Integer courseId, @Param("userId") Integer userId, @Param("roleInCourse") String roleInCourse);
    int checkEnrollmentExists(@Param("courseId") Integer courseId, @Param("userId") Integer userId);
}