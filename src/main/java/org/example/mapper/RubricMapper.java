package org.example.mapper;

import org.example.entity.RubricItemDTO;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface RubricMapper {
    List<RubricItemDTO> selectItemsByAssignmentId(@Param("assignmentId") Integer assignmentId);
    int insertRubric(Map<String, Object> params);
    int insertRubricItem(@Param("rubricId") Integer rubricId,
                         @Param("itemName") String itemName,
                         @Param("maxScore") Double maxScore,
                         @Param("weight") Double weight,
                         @Param("description") String description,
                         @Param("sortOrder") Integer sortOrder);
}
