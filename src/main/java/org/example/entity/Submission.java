package org.example.entity;

import java.util.Date;

public class Submission {
    private Integer submissionId;
    private Integer assignmentId;
    private Integer studentId;
    private String filePath;
    private String fileName;
    private String contentHash;
    private Double similarityPct;
    private String status;
    private Date submittedAt;
    private String contentText;
    private Double finalScore;
    private String finalComment;

    public Integer getSubmissionId() { return submissionId; }
    public void setSubmissionId(Integer submissionId) { this.submissionId = submissionId; }
    public Integer getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Integer assignmentId) { this.assignmentId = assignmentId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public Double getSimilarityPct() { return similarityPct; }
    public void setSimilarityPct(Double similarityPct) { this.similarityPct = similarityPct; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date submittedAt) { this.submittedAt = submittedAt; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }
    public String getFinalComment() { return finalComment; }
    public void setFinalComment(String finalComment) { this.finalComment = finalComment; }
}