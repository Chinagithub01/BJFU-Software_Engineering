package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.ibatis.session.SqlSession;
import org.example.entity.*;
import org.example.mapper.*;
import org.example.util.AttachmentPreviewUtil;
import org.example.util.MyBatisUtil;
import org.example.util.RubricValidator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/reviews")
public class ReviewServlet extends HttpServlet {
    private Gson gson = new Gson();

    // GET: 获取某条互评的详情（Rubric项目 + 被评作业信息 + 匿名标记）
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        String praIdStr = req.getParameter("praId");
        if (praIdStr == null) {
            result.addProperty("success", false);
            result.addProperty("message", "请提供 praId");
            out.print(gson.toJson(result));
            return;
        }

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            PeerReviewMapper prMapper = sqlSession.getMapper(PeerReviewMapper.class);
            ReviewMapper revMapper = sqlSession.getMapper(ReviewMapper.class);
            RubricMapper rubricMapper = sqlSession.getMapper(RubricMapper.class);
            AssignmentMapper assignmentMapper = sqlSession.getMapper(AssignmentMapper.class);

            int praId = Integer.parseInt(praIdStr);
            PeerReviewAssignment pra = prMapper.selectById(praId);
            if (pra == null) {
                result.addProperty("success", false);
                result.addProperty("message", "互评任务不存在");
                out.print(gson.toJson(result));
                return;
            }

            SubmissionMapper subMapper = sqlSession.getMapper(SubmissionMapper.class);
            Submission submission = subMapper.selectSubmissionById(pra.getSubmissionId());

            if ("preview".equals(req.getParameter("mode"))) {
                result.addProperty("success", true);
                result.add("data", AttachmentPreviewUtil.buildPreview(submission, getServletContext()));
                out.print(gson.toJson(result));
                out.flush();
                return;
            }

            Assignment assignment = assignmentMapper.selectAssignmentById(pra.getAssignmentId());
            List<RubricItemDTO> rubricItems = rubricMapper.selectItemsByAssignmentId(pra.getAssignmentId());

            // 检查是否已经提交过互评
            Review existing = revMapper.selectByPraId(praId);

            JsonObject data = new JsonObject();
            data.addProperty("praId", pra.getPraId());
            data.addProperty("assignmentId", pra.getAssignmentId());
            data.addProperty("submissionId", pra.getSubmissionId());
            if (assignment != null) {
                data.addProperty("assignmentTitle", assignment.getTitle());
            }
            // 匿名：不暴露被评者的真实身份，使用编号替代
            data.addProperty("submitterName", "同学" + (pra.getSubmissionId() % 100));
            data.addProperty("status", pra.getStatus());

            if (submission != null) {
                data.addProperty("fileName", submission.getFileName());
                data.addProperty("filePath", submission.getFilePath());
                data.addProperty("contentText", submission.getContentText());
            }
            data.addProperty("reviewSubmitted", existing != null && "submitted".equals(existing.getStatus()));

            if (existing != null && "submitted".equals(existing.getStatus())) {
                data.addProperty("overallComment", existing.getOverallComment());
                data.addProperty("totalScore", existing.getTotalScore());
            }

            // 若无 Rubric，给默认的 0-100 总分项
            if (rubricItems.isEmpty()) {
                RubricItemDTO defaultItem = new RubricItemDTO();
                defaultItem.setItemId(0);
                defaultItem.setItemName("总分");
                defaultItem.setMaxScore(100.0);
                defaultItem.setWeight(1.0);
                defaultItem.setSortOrder(1);
                rubricItems.add(defaultItem);
            }

            JsonArray items = new JsonArray();
            for (RubricItemDTO item : rubricItems) {
                JsonObject itemObj = gson.toJsonTree(item).getAsJsonObject();
                itemObj.addProperty("score", (Double) null);
                itemObj.addProperty("comment", "");
                items.add(itemObj);
            }
            data.add("rubricItems", items);

            result.addProperty("success", true);
            result.add("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", "查询失败：" + e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }

    // POST: 提交互评（Rubric分项打分 + 总评 + 自动汇总总分）
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            int praId = body.get("praId").getAsInt();
            String overallComment = body.has("overallComment") ? body.get("overallComment").getAsString() : "";
            JsonArray scores = body.getAsJsonArray("scores");

            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                ReviewMapper revMapper = sqlSession.getMapper(ReviewMapper.class);
                PeerReviewMapper prMapper = sqlSession.getMapper(PeerReviewMapper.class);

                // 检查是否已提交
                Review existing = revMapper.selectByPraId(praId);
                if (existing != null) {
                    result.addProperty("success", false);
                    result.addProperty("message", "您已提交过该互评，不可重复提交");
                    out.print(gson.toJson(result));
                    return;
                }

                PeerReviewAssignment pra = prMapper.selectById(praId);
                if (pra == null) {
                    result.addProperty("success", false);
                    result.addProperty("message", "互评任务不存在");
                    out.print(gson.toJson(result));
                    return;
                }

                RubricMapper rubricMapper = sqlSession.getMapper(RubricMapper.class);
                List<RubricItemDTO> rubricItems = rubricMapper.selectItemsByAssignmentId(pra.getAssignmentId());

                double totalScore;
                if (rubricItems.isEmpty()) {
                    totalScore = body.has("totalScore") ? body.get("totalScore").getAsDouble() : 0;
                } else {
                    StringBuilder err = new StringBuilder();
                    Double computed = RubricValidator.computeAndValidateReviewTotal(rubricItems, scores, err);
                    if (computed == null) {
                        result.addProperty("success", false);
                        result.addProperty("message", err.toString());
                        out.print(gson.toJson(result));
                        return;
                    }
                    totalScore = computed;
                }

                // 1. 插入 review 记录（服务端按量规重算的加权总分）

                Review review = new Review();
                review.setPraId(praId);
                review.setOverallComment(overallComment);
                review.setTotalScore(totalScore);
                review.setStatus("submitted");
                revMapper.insertReview(review);
                int reviewId = review.getReviewId();

                // 2. 逐项插入评分（跳过虚拟默认项的 itemId=0）
                for (int i = 0; i < scores.size(); i++) {
                    JsonObject s = scores.get(i).getAsJsonObject();
                    int itemId = s.get("rubricItemId").getAsInt();
                    if (itemId == 0) continue;
                    revMapper.insertReviewScore(
                            reviewId,
                            itemId,
                            s.get("score").getAsDouble(),
                            s.has("comment") ? s.get("comment").getAsString() : ""
                    );
                }

                // 3. 更新派评状态为已完成
                prMapper.updateStatus(praId, "completed");

                sqlSession.commit();

                result.addProperty("success", true);
                result.addProperty("message", "互评提交成功！总分：" + String.format("%.1f", totalScore));
                result.addProperty("totalScore", totalScore);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", "提交失败：" + e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }
}
