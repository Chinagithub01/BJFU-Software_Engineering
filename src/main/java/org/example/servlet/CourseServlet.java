package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.ibatis.session.SqlSession;
import org.example.entity.Course;
import org.example.mapper.CourseMapper;
import org.example.util.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/courses")
public class CourseServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        String userIdStr = req.getParameter("userId");
        String roleStr = req.getParameter("role");

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            CourseMapper courseMapper = sqlSession.getMapper(CourseMapper.class);
            List<Course> courses;

            // 如果前端传了明确的用户身份标识，则按身份隔离数据；否则返回空或全部（这里为了安全，若无参数直接返回空数组）
            if (userIdStr != null && !userIdStr.isEmpty() && roleStr != null) {
                int userId = Integer.parseInt(userIdStr);
                courses = courseMapper.selectCoursesByUserId(userId, roleStr);
            } else {
                courses = new java.util.ArrayList<>();
            }
            
            jsonResponse.addProperty("success", true);
            jsonResponse.add("data", gson.toJsonTree(courses));
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "获取课程列表失败：" + e.getMessage());
        }

        out.print(gson.toJson(jsonResponse));
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        BufferedReader reader = req.getReader();
        JsonObject jsonRequest = gson.fromJson(reader, JsonObject.class);

        JsonObject jsonResponse = new JsonObject();

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            CourseMapper courseMapper = sqlSession.getMapper(CourseMapper.class);

            String courseName = jsonRequest.get("courseName").getAsString().trim();
            String courseCode = jsonRequest.get("courseCode").getAsString().trim();
            String semester = jsonRequest.get("semester").getAsString().trim();

            if (courseName.isEmpty() || courseCode.isEmpty() || semester.isEmpty()) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "请填写课程名称、课程代码和学期");
                out.print(gson.toJson(jsonResponse));
                out.flush();
                return;
            }

            Course existing = courseMapper.selectCourseByCode(courseCode);
            if (existing != null) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "课程代码「" + courseCode + "」已存在，请更换后再试");
                out.print(gson.toJson(jsonResponse));
                out.flush();
                return;
            }
            
            Course course = new Course();
            course.setCourseName(courseName);
            course.setCourseCode(courseCode);
            course.setSemester(semester);
            
            if(jsonRequest.has("description") && !jsonRequest.get("description").isJsonNull()) {
                course.setDescription(jsonRequest.get("description").getAsString());
            }
            
            // 从前端传来创建者的 teacherId，如果没有则给默认值 1
            if(jsonRequest.has("teacherId") && !jsonRequest.get("teacherId").isJsonNull()) {
                course.setTeacherId(jsonRequest.get("teacherId").getAsInt());
            } else {
                course.setTeacherId(1); 
            }
            
            course.setIsArchived(0);

            courseMapper.insertCourse(course);
            sqlSession.commit(); // 提交事务

            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "课程创建成功！");
            jsonResponse.add("data", gson.toJsonTree(course));
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            String msg = e.getMessage() != null ? e.getMessage() : "未知错误";
            if (msg.contains("courses_course_code_key") || msg.contains("duplicate key")) {
                jsonResponse.addProperty("message", "课程代码已存在，请更换后再试");
            } else {
                jsonResponse.addProperty("message", "创建课程失败：" + msg);
            }
        }

        out.print(gson.toJson(jsonResponse));
        out.flush();
    }
}
