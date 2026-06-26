package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import org.apache.ibatis.session.SqlSession;
import org.example.util.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet("/api/discussions")
public class DiscussionServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();
        String aidStr = req.getParameter("assignmentId");
        String studentIdStr = req.getParameter("studentId");
        if (aidStr == null) { result.addProperty("success", false); out.print(gson.toJson(result)); return; }

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            java.util.HashMap<String, Object> params = new java.util.HashMap<>();
            params.put("assignmentId", Integer.parseInt(aidStr));
            params.put("studentId", studentIdStr != null ? Integer.parseInt(studentIdStr) : null);
            List<Map<String, Object>> list = sqlSession.selectList(
                    "org.example.mapper.DiscussionMapper.selectByAssignment", params);
            JsonArray arr = new JsonArray();
            for (Map<String, Object> m : list) {
                JsonObject obj = new JsonObject();
                obj.addProperty("discussionId", ((Number) m.get("discussionId")).intValue());
                obj.addProperty("content", (String) m.get("content"));
                obj.addProperty("anonymousName", (String) m.get("anonymousName"));
                Object isMine = m.get("isMine");
                obj.addProperty("isMine", isMine != null && ((Number) isMine).intValue() == 1);
                obj.addProperty("createdAt", m.get("createdAt") != null ? m.get("createdAt").toString() : null);
                arr.add(obj);
            }
            result.addProperty("success", true);
            result.add("data", arr);
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        try {
            JsonObject body = gson.fromJson(new InputStreamReader(req.getInputStream(), "UTF-8"), JsonObject.class);
            int assignmentId = body.get("assignmentId").getAsInt();
            int studentId = body.get("studentId").getAsInt();
            String content = body.get("content").getAsString();

            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                // 检查该学生是否已提交此作业（只有提交者才能参与讨论）
                Object countObj = sqlSession.selectOne(
                        "org.example.mapper.DiscussionMapper.checkSubmission",
                        new java.util.HashMap<String, Object>() {{ put("assignmentId", assignmentId); put("studentId", studentId); }});
                if (countObj == null || ((Number) countObj).intValue() == 0) {
                    result.addProperty("success", false);
                    result.addProperty("message", "您未提交该作业，无法参与讨论");
                    out.print(gson.toJson(result));
                    return;
                }
                sqlSession.insert("org.example.mapper.DiscussionMapper.insert",
                        new java.util.HashMap<String, Object>() {{
                            put("assignmentId", assignmentId);
                            put("studentId", studentId);
                            put("content", content);
                        }});
                sqlSession.commit();
                result.addProperty("success", true);
                result.addProperty("message", "消息已发送");
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
