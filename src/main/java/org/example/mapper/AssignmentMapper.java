package org.example.mapper;

import org.example.entity.Assignment;
import java.util.List;

public interface AssignmentMapper {
    int insertAssignment(Assignment assignment);
    Assignment selectAssignmentById(Integer assignmentId);
    List<Assignment> selectAssignmentsByCourseId(Integer courseId);
    int updateStatus(@org.apache.ibatis.annotations.Param("assignmentId") Integer assignmentId,
                     @org.apache.ibatis.annotations.Param("status") String status);
}
