package org.example.entity;

import java.util.List;

public class ReviewDetailDTO {
    private Integer praId;
    private Integer assignmentId;
    private String assignmentTitle;
    // 被评作业的信息（匿名）
    private String submitterName;    // 匿名显示如 "同学A"
    private String submissionFileName;
    private Integer submissionId;
    // 当前互评状态
    private String reviewStatus;     // pending / completed
    private String overallComment;
    private Double totalScore;
    // 量规评分项
    private List<RubricItemDTO> rubricItems;

    public Integer getPraId() { return praId; }
    public void setPraId(Integer praId) { this.praId = praId; }
    public Integer getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Integer assignmentId) { this.assignmentId = assignmentId; }
    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
    public String getSubmitterName() { return submitterName; }
    public void setSubmitterName(String submitterName) { this.submitterName = submitterName; }
    public String getSubmissionFileName() { return submissionFileName; }
    public void setSubmissionFileName(String submissionFileName) { this.submissionFileName = submissionFileName; }
    public Integer getSubmissionId() { return submissionId; }
    public void setSubmissionId(Integer submissionId) { this.submissionId = submissionId; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public String getOverallComment() { return overallComment; }
    public void setOverallComment(String overallComment) { this.overallComment = overallComment; }
    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }
    public List<RubricItemDTO> getRubricItems() { return rubricItems; }
    public void setRubricItems(List<RubricItemDTO> rubricItems) { this.rubricItems = rubricItems; }
}
