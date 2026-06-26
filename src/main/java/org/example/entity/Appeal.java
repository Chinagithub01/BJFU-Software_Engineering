package org.example.entity;

import java.util.Date;

public class Appeal {
    private Integer appealId;
    private Integer praId;
    private Integer submissionId;
    private Integer studentId;
    private String reason;
    private String status;        // pending / accepted / rejected
    private Integer handlerId;
    private String handlerResponse;
    private Double adjustedScore;
    private Date createdAt;
    private Date resolvedAt;

    // 展示用字段
    private String studentName;
    private String assignmentTitle;
    private String courseName;
    private Double originalScore;

    public Integer getAppealId() { return appealId; }
    public void setAppealId(Integer appealId) { this.appealId = appealId; }
    public Integer getPraId() { return praId; }
    public void setPraId(Integer praId) { this.praId = praId; }
    public Integer getSubmissionId() { return submissionId; }
    public void setSubmissionId(Integer submissionId) { this.submissionId = submissionId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getHandlerId() { return handlerId; }
    public void setHandlerId(Integer handlerId) { this.handlerId = handlerId; }
    public String getHandlerResponse() { return handlerResponse; }
    public void setHandlerResponse(String handlerResponse) { this.handlerResponse = handlerResponse; }
    public Double getAdjustedScore() { return adjustedScore; }
    public void setAdjustedScore(Double adjustedScore) { this.adjustedScore = adjustedScore; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Date resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Double getOriginalScore() { return originalScore; }
    public void setOriginalScore(Double originalScore) { this.originalScore = originalScore; }
}
