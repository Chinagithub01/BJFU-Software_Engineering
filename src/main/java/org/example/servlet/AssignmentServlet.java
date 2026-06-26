package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.ibatis.session.SqlSession;
import org.example.entity.Assignment;
import org.example.mapper.AssignmentMapper;
import org.example.util.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet("/api/assignments")
public class AssignmentServlet extends HttpServlet {
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
            AssignmentMapper assignmentMapper = sqlSession.getMapper(AssignmentMapper.class);
            List<Assignment> assignments = assignmentMapper.selectAssignmentsByCourseId(Integer.parseInt(courseIdStr));
            
            jsonResponse.addProperty("success", true);
            jsonResponse.add("data", gson.toJsonTree(assignments));
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "获取作业列表失败：" + e.getMessage());
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
            AssignmentMapper assignmentMapper = sqlSession.getMapper(AssignmentMapper.class);
            
            Assignment assignment = new Assignment();
            assignment.setCourseId(jsonRequest.get("courseId").getAsInt());
            assignment.setTitle(jsonRequest.get("title").getAsString());
            if(jsonRequest.has("description") && !jsonRequest.get("description").isJsonNull()) {
                assignment.setDescription(jsonRequest.get("description").getAsString());
            }
            
            // 从前端接收精确到分钟的截止时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            if (jsonRequest.has("dueDate") && !jsonRequest.get("dueDate").isJsonNull()) {
                assignment.setDueDate(sdf.parse(jsonRequest.get("dueDate").getAsString()));
            } else {
                assignment.setDueDate(new Date(System.currentTimeMillis() + 7 * 86400000L));
            }
            if (jsonRequest.has("reviewDueDate") && !jsonRequest.get("reviewDueDate").isJsonNull()) {
                assignment.setReviewDueDate(sdf.parse(jsonRequest.get("reviewDueDate").getAsString()));
            } else {
                assignment.setReviewDueDate(new Date(System.currentTimeMillis() + 10 * 86400000L));
            }
            
            assignment.setFileTypes("pdf,doc,docx");
            assignment.setMaxFileSizeMb(20);
            assignment.setPeerReviewCount(3);
            assignment.setStatus("published");
            assignment.setCreatedBy(jsonRequest.has("createdBy") ? jsonRequest.get("createdBy").getAsInt() : 1);

            assignmentMapper.insertAssignment(assignment);
            sqlSession.commit(); // 提交事务

            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "作业发布成功！");
            jsonResponse.add("data", gson.toJsonTree(assignment));
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "发布作业失败：" + e.getMessage());
        }

        out.print(gson.toJson(jsonResponse));
        out.flush();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            int assignmentId = body.get("assignmentId").getAsInt();
            String status = body.get("status").getAsString();

            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                AssignmentMapper mapper = sqlSession.getMapper(AssignmentMapper.class);
                mapper.updateStatus(assignmentId, status);
                sqlSession.commit();
                result.addProperty("success", true);
                result.addProperty("message", "操作成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }
}
