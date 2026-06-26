package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.ibatis.session.SqlSession;
import org.example.entity.Course;
import org.example.mapper.CourseMapper;
import org.example.mapper.EnrollmentMapper;
import org.example.util.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/courses/join")
public class JoinCourseServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        BufferedReader reader = req.getReader();
        JsonObject jsonRequest = gson.fromJson(reader, JsonObject.class);

        JsonObject jsonResponse = new JsonObject();

        if (!jsonRequest.has("courseCode") || !jsonRequest.has("userId")) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "缺少必要的参数");
            out.print(gson.toJson(jsonResponse));
            return;
        }

        String courseCode = jsonRequest.get("courseCode").getAsString();
        int userId = jsonRequest.get("userId").getAsInt();

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            CourseMapper courseMapper = sqlSession.getMapper(CourseMapper.class);
            EnrollmentMapper enrollmentMapper = sqlSession.getMapper(EnrollmentMapper.class);

            // 1. 查找课程是否存在
            Course course = courseMapper.selectCourseByCode(courseCode);
            if (course == null) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "无效的邀请码，找不到对应课程");
            } else {
                // 2. 检查是否已经加入
                int count = enrollmentMapper.checkEnrollmentExists(course.getCourseId(), userId);
                if (count > 0) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "您已经加入了该课程");
                } else {
                    // 3. 插入选课记录，默认以 student 身份加入
                    enrollmentMapper.insertEnrollment(course.getCourseId(), userId, "student");
                    sqlSession.commit();
                    
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "成功加入课程！");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "加入课程失败：" + e.getMessage());
        }

        out.print(gson.toJson(jsonResponse));
        out.flush();
    }
}
