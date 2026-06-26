package org.example.mapper;

import org.example.entity.PeerReviewAssignment;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PeerReviewMapper {
    int insertPeerReview(PeerReviewAssignment pra);
    List<PeerReviewAssignment> selectByReviewerId(@Param("reviewerId") Integer reviewerId);
    PeerReviewAssignment selectById(@Param("praId") Integer praId);
    int updateStatus(@Param("praId") Integer praId, @Param("status") String status);
    int countByAssignmentId(@Param("assignmentId") Integer assignmentId);
    List<PeerReviewAssignment> selectByAssignmentId(@Param("assignmentId") Integer assignmentId);
    // 查询学生的提交收到了哪些互评
    List<PeerReviewAssignment> selectReviewsReceived(@Param("studentId") Integer studentId);
}
