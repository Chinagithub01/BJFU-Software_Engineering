package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import org.apache.ibatis.session.SqlSession;
import org.example.entity.Appeal;
import org.example.mapper.AppealMapper;
import org.example.mapper.SubmissionMapper;
import org.example.util.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/appeals")
public class AppealServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        String studentIdStr = req.getParameter("studentId");
        String mode = req.getParameter("mode");
        String teacherIdStr = req.getParameter("teacherId");

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            AppealMapper mapper = sqlSession.getMapper(AppealMapper.class);
            List<Appeal> list;
            if (teacherIdStr != null) {
                list = mapper.selectByTeacherId(Integer.parseInt(teacherIdStr));
            } else if ("all".equals(mode)) {
                list = mapper.selectAllPending();
            } else if (studentIdStr != null) {
                list = mapper.selectByStudentId(Integer.parseInt(studentIdStr));
            } else {
                list = java.util.Collections.emptyList();
            }
            JsonArray arr = new JsonArray();
            for (Appeal a : list) {
                JsonObject obj = new JsonObject();
                obj.addProperty("appealId", a.getAppealId());
                obj.addProperty("praId", a.getPraId());
                obj.addProperty("submissionId", a.getSubmissionId());
                obj.addProperty("studentId", a.getStudentId());
                obj.addProperty("studentName", a.getStudentName());
                obj.addProperty("assignmentTitle", a.getAssignmentTitle());
                obj.addProperty("courseName", a.getCourseName());
                obj.addProperty("reason", a.getReason());
                obj.addProperty("status", a.getStatus());
                obj.addProperty("originalScore", a.getOriginalScore());
                obj.addProperty("adjustedScore", a.getAdjustedScore());
                obj.addProperty("handlerResponse", a.getHandlerResponse());
                obj.addProperty("createdAt", a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
                obj.addProperty("resolvedAt", a.getResolvedAt() != null ? a.getResolvedAt().toString() : null);
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
            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                AppealMapper mapper = sqlSession.getMapper(AppealMapper.class);
                Appeal appeal = new Appeal();
                appeal.setPraId(body.get("praId").getAsInt());
                appeal.setSubmissionId(body.get("submissionId").getAsInt());
                appeal.setStudentId(body.get("studentId").getAsInt());
                appeal.setReason(body.get("reason").getAsString());
                mapper.insertAppeal(appeal);
                sqlSession.commit();
                result.addProperty("success", true);
                result.addProperty("message", "申诉已提交，等待教师处理");
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
                AppealMapper mapper = sqlSession.getMapper(AppealMapper.class);
                Double adjScore = null;
                if (body.has("adjustedScore") && !body.get("adjustedScore").isJsonNull()) {
                    adjScore = body.get("adjustedScore").getAsDouble();
                }
                String handlerResp = null;
                if (body.has("handlerResponse") && !body.get("handlerResponse").isJsonNull()) {
                    handlerResp = body.get("handlerResponse").getAsString();
                }
                int appealId = body.get("appealId").getAsInt();
                String status = body.get("status").getAsString();
                mapper.resolveAppeal(appealId, status, body.get("handlerId").getAsInt(), handlerResp, adjScore);

                // 如果是同意申诉且有调整分数，同步更新提交的最终分数
                if ("accepted".equals(status) && adjScore != null) {
                    Appeal appeal = mapper.selectById(appealId);
                    if (appeal != null) {
                        SubmissionMapper subMapper = sqlSession.getMapper(SubmissionMapper.class);
                        subMapper.setFinalScore(appeal.getSubmissionId(), adjScore, handlerResp);
                    }
                }

                sqlSession.commit();
                result.addProperty("success", true);
                result.addProperty("message", "申诉已处理");
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
