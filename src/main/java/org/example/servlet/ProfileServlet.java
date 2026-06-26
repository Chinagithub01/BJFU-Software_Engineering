package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import org.apache.ibatis.session.SqlSession;
import org.example.entity.User;
import org.example.mapper.ProfileMapper;
import org.example.mapper.UserMapper;
import org.example.util.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/api/profile")
public class ProfileServlet extends HttpServlet {
    private Gson gson = new Gson();

    private static String roleFromUser(User user) {
        if (user == null || user.getRoleId() == null) {
            return "student";
        }
        switch (user.getRoleId()) {
            case 1: return "admin";
            case 2: return "teacher";
            case 3: return "ta";
            default: return "student";
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        String userIdStr = req.getParameter("userId");
        if (userIdStr == null) {
            result.addProperty("success", false);
            result.addProperty("message", "请提供 userId");
            out.print(gson.toJson(result));
            return;
        }

        boolean includeSummary = "true".equalsIgnoreCase(req.getParameter("includeSummary"));

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            UserMapper mapper = sqlSession.getMapper(UserMapper.class);
            User user = mapper.selectUserById(Integer.parseInt(userIdStr));
            if (user != null) {
                String roleStr = roleFromUser(user);
                JsonObject data = new JsonObject();
                data.addProperty("userId", user.getUserId());
                data.addProperty("username", user.getUsername());
                data.addProperty("nickname", user.getNickname());
                data.addProperty("phone", user.getPhone());
                data.addProperty("email", user.getEmail());
                data.addProperty("school", user.getSchool());
                data.addProperty("className", user.getClassName());
                data.addProperty("realName", user.getRealName());
                data.addProperty("studentId", user.getStudentId());
                data.addProperty("role", roleStr);

                if (includeSummary) {
                    JsonObject summary = new JsonObject();
                    ProfileMapper profileMapper = sqlSession.getMapper(ProfileMapper.class);
                    if ("admin".equals(roleStr)) {
                        Map<String, Object> stats = sqlSession.selectOne(
                                "org.example.mapper.AdminMapper.platformStats");
                        if (stats != null) {
                            for (Map.Entry<String, Object> e : stats.entrySet()) {
                                summary.addProperty(e.getKey(), toLong(e.getValue()));
                            }
                        }
                    } else if ("teacher".equals(roleStr)) {
                        Map<String, Object> stats = profileMapper.teacherSummary(user.getUserId());
                        if (stats != null) {
                            summary.addProperty("courseCount", toLong(stats.get("courseCount")));
                            summary.addProperty("publishedAssignments", toLong(stats.get("publishedAssignments")));
                            summary.addProperty("pendingAppeals", toLong(stats.get("pendingAppeals")));
                        }
                    } else {
                        Map<String, Object> stats = profileMapper.studentSummary(user.getUserId());
                        if (stats != null) {
                            summary.addProperty("courseCount", toLong(stats.get("courseCount")));
                            summary.addProperty("pendingReviews", toLong(stats.get("pendingReviews")));
                            summary.addProperty("submissionCount", toLong(stats.get("submissionCount")));
                        }
                    }
                    data.add("summary", summary);
                }

                result.addProperty("success", true);
                result.add("data", data);
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "用户不存在");
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
            int userId = body.get("userId").getAsInt();

            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                UserMapper mapper = sqlSession.getMapper(UserMapper.class);
                User existing = mapper.selectUserById(userId);
                if (existing == null) {
                    result.addProperty("success", false);
                    result.addProperty("message", "用户不存在");
                    out.print(gson.toJson(result));
                    return;
                }

                String role = roleFromUser(existing);
                User user = new User();
                user.setUserId(userId);
                user.setNickname(body.has("nickname") ? body.get("nickname").getAsString() : null);
                user.setPhone(body.has("phone") ? body.get("phone").getAsString() : null);
                user.setEmail(body.has("email") ? body.get("email").getAsString() : null);

                if ("student".equals(role) || "ta".equals(role)) {
                    user.setSchool(body.has("school") ? body.get("school").getAsString() : null);
                    user.setClassName(body.has("className") ? body.get("className").getAsString() : null);
                    user.setStudentId(body.has("studentId") ? body.get("studentId").getAsString() : null);
                } else {
                    user.setSchool(body.has("school") ? body.get("school").getAsString() : null);
                    user.setClassName(null);
                    user.setStudentId(null);
                }

                mapper.updateProfile(user);
                sqlSession.commit();
                result.addProperty("success", true);
                result.addProperty("message", "个人信息已更新");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
