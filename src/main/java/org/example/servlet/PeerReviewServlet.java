package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.ibatis.session.SqlSession;
import org.example.entity.Assignment;
import org.example.entity.Course;
import org.example.entity.PeerReviewAssignment;
import org.example.entity.Review;
import org.example.entity.Submission;
import org.example.mapper.AssignmentMapper;
import org.example.mapper.CourseMapper;
import org.example.mapper.PeerReviewMapper;
import org.example.mapper.ReviewMapper;
import org.example.mapper.SubmissionMapper;
import org.example.util.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/api/reviews/assign")
public class PeerReviewServlet extends HttpServlet {
    private Gson gson = new Gson();

    // POST: 教师触发派评，为某作业的所有学生随机分配互评任务
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            int assignmentId = body.get("assignmentId").getAsInt();
            int reviewCount = body.has("peerReviewCount") ? body.get("peerReviewCount").getAsInt() : 3;

            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                SubmissionMapper subMapper = sqlSession.getMapper(SubmissionMapper.class);
                PeerReviewMapper prMapper = sqlSession.getMapper(PeerReviewMapper.class);

                // 1. 检查是否已分配过
                int count = prMapper.countByAssignmentId(assignmentId);
                if (count > 0) {
                    result.addProperty("success", false);
                    result.addProperty("message", "该作业已分配过互评任务，请勿重复操作");
                    out.print(gson.toJson(result));
                    return;
                }

                // 2. 获取所有提交
                List<Submission> submissions = subMapper.selectSubmissionsByAssignment(assignmentId);
                if (submissions.size() < 2) {
                    result.addProperty("success", false);
                    result.addProperty("message", "提交人数不足（至少2人），无法进行互评分配");
                    out.print(gson.toJson(result));
                    return;
                }

                int studentCount = submissions.size();
                // 每个人实际分配的互评数量不能超过总人数-1
                int actualReviewCount = Math.min(reviewCount, studentCount - 1);

                // 3. 构建映射
                Map<Integer, Integer> studentToSub = new LinkedHashMap<>();
                Map<Integer, Integer> subToStudent = new LinkedHashMap<>();
                for (Submission s : submissions) {
                    studentToSub.put(s.getStudentId(), s.getSubmissionId());
                    subToStudent.put(s.getSubmissionId(), s.getStudentId());
                }
                List<Integer> studentIds = new ArrayList<>(studentToSub.keySet());
                List<Integer> submissionIds = new ArrayList<>(subToStudent.keySet());

                // 4. 以提交为中心：每份提交至少2人评，每人最多评3份
                Random rng = new Random();
                Set<String> assignedPairs = new HashSet<>();
                Map<Integer, Integer> reviewerCount = new HashMap<>();
                int minReviewsPerSub = Math.min(2, studentIds.size() - 1);
                int maxReviewsPerReviewer = 3;

                for (int submissionId : submissionIds) {
                    int targetStudentId = subToStudent.get(submissionId);
                    List<Integer> candidates = new ArrayList<>(studentIds);
                    candidates.remove(Integer.valueOf(targetStudentId)); // 排除提交者自己
                    Collections.shuffle(candidates, rng);

                    int got = 0;
                    for (int reviewerId : candidates) {
                        if (got >= 2) break; // 已满2份

                        int cnt = reviewerCount.getOrDefault(reviewerId, 0);
                        if (cnt >= maxReviewsPerReviewer) continue; // 该评阅人已满3份

                        String pairKey = reviewerId + "_" + submissionId;
                        String reverseKey = submissionId + "_" + reviewerId;
                        if (assignedPairs.contains(pairKey) || assignedPairs.contains(reverseKey)) continue;

                        PeerReviewAssignment pra = new PeerReviewAssignment();
                        pra.setAssignmentId(assignmentId);
                        pra.setReviewerId(reviewerId);
                        pra.setSubmissionId(submissionId);
                        pra.setStatus("pending");
                        prMapper.insertPeerReview(pra);

                        assignedPairs.add(pairKey);
                        reviewerCount.put(reviewerId, cnt + 1);
                        got++;
                    }
                }

                int assignedCount = assignedPairs.size();
                sqlSession.commit();

                JsonObject data = new JsonObject();
                data.addProperty("totalAssigned", assignedCount);
                data.addProperty("studentsCount", studentIds.size());
                data.addProperty("minReviewsPerSub", minReviewsPerSub);
                data.addProperty("maxPerReviewer", maxReviewsPerReviewer);
                result.add("data", data);
                result.addProperty("success", true);
                result.addProperty("message", "派评完成！共 " + assignedCount + " 条互评任务");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", "派评失败：" + e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }

    // GET: reviewerId= 查待评任务, studentId= 查收到的互评结果
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        String reviewerIdStr = req.getParameter("reviewerId");
        String studentIdStr = req.getParameter("studentId");
        String assignmentIdStr = req.getParameter("assignmentId");

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            PeerReviewMapper prMapper = sqlSession.getMapper(PeerReviewMapper.class);
            ReviewMapper revMapper = sqlSession.getMapper(ReviewMapper.class);
            SubmissionMapper subMapper = sqlSession.getMapper(SubmissionMapper.class);
            AssignmentMapper assignmentMapper = sqlSession.getMapper(AssignmentMapper.class);

            if (reviewerIdStr != null) {
                // 查询待评任务
                CourseMapper courseMapper = sqlSession.getMapper(CourseMapper.class);
                List<PeerReviewAssignment> assignments = prMapper.selectByReviewerId(Integer.parseInt(reviewerIdStr));
                JsonArray list = new JsonArray();
                for (PeerReviewAssignment pra : assignments) {
                    JsonObject item = new JsonObject();
                    item.addProperty("praId", pra.getPraId());
                    item.addProperty("assignmentId", pra.getAssignmentId());
                    item.addProperty("status", pra.getStatus());
                    Submission sub = subMapper.selectSubmissionById(pra.getSubmissionId());
                    if (sub != null) item.addProperty("fileName", sub.getFileName());
                    Assignment asgn = assignmentMapper.selectAssignmentById(pra.getAssignmentId());
                    if (asgn != null) {
                        item.addProperty("assignmentTitle", asgn.getTitle());
                        Course course = courseMapper.selectCourseById(asgn.getCourseId());
                        if (course != null) item.addProperty("courseName", course.getCourseName());
                    }
                    Review existing = revMapper.selectByPraId(pra.getPraId());
                    item.addProperty("reviewSubmitted", existing != null && "submitted".equals(existing.getStatus()));
                    if (existing != null) item.addProperty("totalScore", existing.getTotalScore());
                    list.add(item);
                }
                result.addProperty("success", true);
                result.add("data", list);
            } else if (studentIdStr != null) {
                // 查询收到的互评结果
                List<PeerReviewAssignment> received = prMapper.selectReviewsReceived(Integer.parseInt(studentIdStr));
                JsonArray list = new JsonArray();
                for (PeerReviewAssignment pra : received) {
                    Submission sub = subMapper.selectSubmissionById(pra.getSubmissionId());
                    Review review = revMapper.selectByPraId(pra.getPraId());
                    Assignment assignment = assignmentMapper.selectAssignmentById(pra.getAssignmentId());
                    if (review == null) continue;

                    JsonObject item = new JsonObject();
                    item.addProperty("praId", pra.getPraId());
                    item.addProperty("assignmentId", pra.getAssignmentId());
                    item.addProperty("submissionId", pra.getSubmissionId());
                    item.addProperty("overallComment", review.getOverallComment());
                    item.addProperty("totalScore", review.getTotalScore());
                    if (assignment != null) item.addProperty("assignmentTitle", assignment.getTitle());
                    if (sub != null) item.addProperty("fileName", sub.getFileName());

                    // 逐项评分
                    List<java.util.Map<String, Object>> scores = revMapper.selectScoresByReviewId(review.getReviewId());
                    JsonArray scoreArr = new JsonArray();
                    for (java.util.Map<String, Object> s : scores) {
                        JsonObject sc = new JsonObject();
                        sc.addProperty("itemName", (String) s.get("itemName"));
                        sc.addProperty("score", ((Number) s.get("score")).doubleValue());
                        sc.addProperty("maxScore", ((Number) s.get("maxScore")).doubleValue());
                        sc.addProperty("comment", (String) s.get("comment"));
                        scoreArr.add(sc);
                    }
                    item.add("scores", scoreArr);
                    list.add(item);
                }
                result.addProperty("success", true);
                result.add("data", list);
            } else if (assignmentIdStr != null) {
                List<PeerReviewAssignment> all = prMapper.selectByAssignmentId(Integer.parseInt(assignmentIdStr));
                JsonArray list = new JsonArray();
                for (PeerReviewAssignment pra : all) {
                    Review review = revMapper.selectByPraId(pra.getPraId());
                    Submission sub = subMapper.selectSubmissionById(pra.getSubmissionId());
                    JsonObject item = new JsonObject();
                    item.addProperty("praId", pra.getPraId());
                    item.addProperty("assignmentId", pra.getAssignmentId());
                    item.addProperty("reviewerId", pra.getReviewerId());
                    item.addProperty("submissionId", pra.getSubmissionId());
                    item.addProperty("status", pra.getStatus());
                    if (review != null) {
                        item.addProperty("totalScore", review.getTotalScore());
                        item.addProperty("overallComment", review.getOverallComment());
                    }
                    if (sub != null) item.addProperty("fileName", sub.getFileName());
                    list.add(item);
                }
                result.addProperty("success", true);
                result.add("data", list);
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "请提供 reviewerId 或 studentId");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", "查询失败：" + e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }
}
