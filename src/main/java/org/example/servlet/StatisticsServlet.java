package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

@WebServlet("/api/statistics")
public class StatisticsServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        String assignmentIdStr = req.getParameter("assignmentId");
        if (assignmentIdStr == null) {
            result.addProperty("success", false);
            result.addProperty("message", "请提供 assignmentId");
            out.print(gson.toJson(result));
            return;
        }
        int assignmentId = Integer.parseInt(assignmentIdStr);

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            JsonObject data = new JsonObject();

            // 1. 分数分布
            List<Map<String, Object>> distribution = sqlSession.selectList(
                    "org.example.mapper.StatisticsMapper.scoreDistribution", assignmentId);
            data.add("distribution", gson.toJsonTree(distribution));

            // 2. 互评一致性（异常检测）
            List<Map<String, Object>> anomalies = sqlSession.selectList(
                    "org.example.mapper.StatisticsMapper.reviewConsistency", assignmentId);
            data.add("anomalies", gson.toJsonTree(anomalies));

            // 3. 学生成绩单
            List<Map<String, Object>> scores = sqlSession.selectList(
                    "org.example.mapper.StatisticsMapper.studentScores", assignmentId);
            data.add("studentScores", gson.toJsonTree(scores));

            // 教师最终评分分布
            List<Map<String, Object>> finalDist = sqlSession.selectList(
                    "org.example.mapper.StatisticsMapper.finalScoreDistribution", assignmentId);
            data.add("finalDistribution", gson.toJsonTree(finalDist));

            result.addProperty("success", true);
            result.add("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", "查询统计失败：" + e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }
}
