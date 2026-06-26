package org.example.entity;

import java.util.Date;

public class PeerReviewAssignment {
    private Integer praId;
    private Integer assignmentId;
    private Integer reviewerId;
    private Integer submissionId;
    private String status;       // pending / completed
    private Date assignedAt;

    // Getters and Setters
    public Integer getPraId() { return praId; }
    public void setPraId(Integer praId) { this.praId = praId; }
    public Integer getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Integer assignmentId) { this.assignmentId = assignmentId; }
    public Integer getReviewerId() { return reviewerId; }
    public void setReviewerId(Integer reviewerId) { this.reviewerId = reviewerId; }
    public Integer getSubmissionId() { return submissionId; }
    public void setSubmissionId(Integer submissionId) { this.submissionId = submissionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Date assignedAt) { this.assignedAt = assignedAt; }
}
