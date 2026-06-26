package org.example.mapper;

import org.example.entity.Submission;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SubmissionMapper {
    int insertSubmission(Submission submission);
    Submission selectSubmissionByAssignmentAndStudent(@Param("assignmentId") Integer assignmentId, @Param("studentId") Integer studentId);
    List<Submission> selectSubmissionsByAssignment(Integer assignmentId);
    int countSubmissionsByAssignment(@Param("assignmentId") Integer assignmentId);
    List<Submission> selectSubmissionsByAssignmentPaged(
            @Param("assignmentId") Integer assignmentId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);
    Submission selectSubmissionById(@Param("submissionId") Integer submissionId);
    int setFinalScore(@Param("submissionId") Integer submissionId,
                      @Param("finalScore") Double finalScore,
                      @Param("finalComment") String finalComment);
}