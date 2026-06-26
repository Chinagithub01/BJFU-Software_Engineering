package org.example.entity;

import java.util.Date;

public class Review {
    private Integer reviewId;
    private Integer praId;
    private String overallComment;
    private Double totalScore;
    private String status;       // draft / submitted
    private Date submittedAt;

    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }
    public Integer getPraId() { return praId; }
    public void setPraId(Integer praId) { this.praId = praId; }
    public String getOverallComment() { return overallComment; }
    public void setOverallComment(String overallComment) { this.overallComment = overallComment; }
    public Double getTotalScore() { return totalScore; }
    public void setTotalScore(Double totalScore) { this.totalScore = totalScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }
}
