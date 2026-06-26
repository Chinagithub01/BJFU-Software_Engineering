package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import org.apache.ibatis.session.SqlSession;
import org.example.entity.*;
import org.example.mapper.*;
import org.example.util.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();
        String path = req.getPathInfo();

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            if ("/users".equals(path)) {
                String pageStr = req.getParameter("page");
                if (pageStr != null && !pageStr.isEmpty()) {
                    int page = Math.max(1, Integer.parseInt(pageStr));
                    int pageSize = 10;
                    String pageSizeStr = req.getParameter("pageSize");
                    if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                        pageSize = Math.min(100, Math.max(1, Integer.parseInt(pageSizeStr)));
                    }
                    int total = sqlSession.selectOne("org.example.mapper.AdminMapper.countUsers");
                    int offset = (page - 1) * pageSize;
                    java.util.Map<String, Object> params = new java.util.HashMap<>();
                    params.put("offset", offset);
                    params.put("pageSize", pageSize);
                    List<java.util.Map<String, Object>> users =
                            sqlSession.selectList("org.example.mapper.AdminMapper.listUsersPaged", params);
                    result.addProperty("success", true);
                    result.add("data", gson.toJsonTree(users));
                    result.addProperty("total", total);
                    result.addProperty("page", page);
                    result.addProperty("pageSize", pageSize);
                } else {
                    List<java.util.Map<String, Object>> users =
                            sqlSession.selectList("org.example.mapper.AdminMapper.listUsers");
                    result.addProperty("success", true);
                    result.add("data", gson.toJsonTree(users));
                }
            } else if ("/courses".equals(path)) {
                List<java.util.Map<String, Object>> courses = sqlSession.selectList("org.example.mapper.AdminMapper.listCourses");
                result.addProperty("success", true);
                result.add("data", gson.toJsonTree(courses));
            } else if ("/stats".equals(path)) {
                java.util.Map<String, Object> stats = sqlSession.selectOne("org.example.mapper.AdminMapper.platformStats");
                result.addProperty("success", true);
                result.add("data", gson.toJsonTree(stats));
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "未知路径");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        try {
            JsonObject body = gson.fromJson(new InputStreamReader(req.getInputStream(), "UTF-8"), JsonObject.class);
            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                String path = req.getPathInfo();
                if ("/users".equals(path)) {
                    sqlSession.update("org.example.mapper.AdminMapper.updateUser", body);
                    sqlSession.commit();
                    result.addProperty("success", true);
                    result.addProperty("message", "用户已更新");
                } else {
                    result.addProperty("success", false);
                    result.addProperty("message", "未知路径");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        try {
            String path = req.getPathInfo();
            String id = req.getParameter("id");
            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                if ("/courses".equals(path)) {
                    int cid = Integer.parseInt(id);
                    // 逐层删除关联数据
                    sqlSession.delete("org.example.mapper.AdminMapper.deleteCourseEnrollments", cid);
                    sqlSession.delete("org.example.mapper.AdminMapper.deleteAppealsByCourse", cid);
                    sqlSession.delete("org.example.mapper.AdminMapper.deleteReviewScoresByCourse", cid);
                    sqlSession.delete("org.example.mapper.AdminMapper.deleteReviewsByCourse", cid);
                    sqlSession.delete("org.example.mapper.AdminMapper.deletePeerReviewsByCourse", cid);
                    sqlSession.delete("org.example.mapper.AdminMapper.deleteRubricItemsByCourse", cid);
                    sqlSession.delete("org.example.mapper.AdminMapper.deleteRubricsByCourse", cid);
                    sqlSession.delete("org.example.mapper.AdminMapper.deleteSubmissionsByCourse", cid);
                    sqlSession.delete("org.example.mapper.AdminMapper.deleteAssignmentsByCourse", cid);
                    sqlSession.delete("org.example.mapper.AdminMapper.deleteCourse", cid);
                    sqlSession.commit();
                    result.addProperty("success", true);
                    result.addProperty("message", "课程已删除");
                } else {
                    result.addProperty("success", false);
                    result.addProperty("message", "未知路径");
                }
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
