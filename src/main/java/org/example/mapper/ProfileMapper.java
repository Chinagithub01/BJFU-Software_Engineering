package org.example.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface ProfileMapper {
    Map<String, Object> studentSummary(@Param("userId") int userId);

    Map<String, Object> teacherSummary(@Param("teacherId") int teacherId);
}
