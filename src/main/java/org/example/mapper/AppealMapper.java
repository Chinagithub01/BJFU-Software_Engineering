package org.example.mapper;

import org.example.entity.Appeal;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AppealMapper {
    int insertAppeal(Appeal appeal);
    List<Appeal> selectByStudentId(@Param("studentId") Integer studentId);
    List<Appeal> selectAllPending();
    List<Appeal> selectByTeacherId(@Param("teacherId") Integer teacherId);
    Appeal selectById(@Param("appealId") Integer appealId);
    int resolveAppeal(@Param("appealId") Integer appealId,
                      @Param("status") String status,
                      @Param("handlerId") Integer handlerId,
                      @Param("handlerResponse") String handlerResponse,
                      @Param("adjustedScore") Double adjustedScore);
}
