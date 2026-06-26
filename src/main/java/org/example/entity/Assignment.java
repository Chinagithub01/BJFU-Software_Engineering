package org.example.entity;

import java.util.Date;

public class Assignment {
    private Integer assignmentId;
    private Integer courseId;
    private String title;
    private String description;
    private Date dueDate;
    private Date reviewDueDate;
    private String fileTypes;
    private Integer maxFileSizeMb;
    private Integer peerReviewCount;
    private String status;
    private Integer createdBy;
    private Date createdAt;

    // Getters and Setters
    public Integer getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Integer assignmentId) { this.assignmentId = assignmentId; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Date getReviewDueDate() { return reviewDueDate; }
    public void setReviewDueDate(Date reviewDueDate) { this.reviewDueDate = reviewDueDate; }

    public String getFileTypes() { return fileTypes; }
    public void setFileTypes(String fileTypes) { this.fileTypes = fileTypes; }

    public Integer getMaxFileSizeMb() { return maxFileSizeMb; }
    public void setMaxFileSizeMb(Integer maxFileSizeMb) { this.maxFileSizeMb = maxFileSizeMb; }

    public Integer getPeerReviewCount() { return peerReviewCount; }
    public void setPeerReviewCount(Integer peerReviewCount) { this.peerReviewCount = peerReviewCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Assignment{" +
                "assignmentId=" + assignmentId +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
