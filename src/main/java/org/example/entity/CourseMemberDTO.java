package org.example.entity;

import java.util.Date;

// 用于传输课程成员名单的数据传输对象
public class CourseMemberDTO {
    private Integer userId;
    private String username;
    private String realName;
    private String roleInCourse;
    private Date enrolledAt;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getRoleInCourse() { return roleInCourse; }
    public void setRoleInCourse(String roleInCourse) { this.roleInCourse = roleInCourse; }
    public Date getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Date enrolledAt) { this.enrolledAt = enrolledAt; }
}