package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.ibatis.session.SqlSession;
import org.example.entity.CourseMemberDTO;
import org.example.mapper.EnrollmentMapper;
import org.example.util.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/enrollments")
public class EnrollmentServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        String courseIdStr = req.getParameter("courseId");
        if (courseIdStr == null || courseIdStr.isEmpty()) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "请提供 courseId");
            out.print(gson.toJson(jsonResponse));
            return;
        }

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            EnrollmentMapper mapper = sqlSession.getMapper(EnrollmentMapper.class);
            List<CourseMemberDTO> members = mapper.selectMembersByCourseId(Integer.parseInt(courseIdStr));
            
            jsonResponse.addProperty("success", true);
            jsonResponse.add("data", gson.toJsonTree(members));
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "获取名单失败：" + e.getMessage());
        }

        out.print(gson.toJson(jsonResponse));
        out.flush();
    }
}