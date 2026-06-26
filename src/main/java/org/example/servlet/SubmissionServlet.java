package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.ibatis.session.SqlSession;
import org.example.entity.Assignment;
import org.example.entity.PeerReviewAssignment;
import org.example.entity.Review;
import org.example.entity.Submission;
import org.example.entity.User;
import org.example.mapper.AppealMapper;
import org.example.mapper.AssignmentMapper;
import org.example.mapper.PeerReviewMapper;
import org.example.mapper.ReviewMapper;
import org.example.mapper.SubmissionMapper;
import org.example.mapper.UserMapper;
import org.example.storage.ObjectStorageFactory;
import org.example.storage.ObjectStorageService;
import org.example.storage.StoredObject;
import org.example.util.AttachmentPreviewUtil;
import org.example.util.MyBatisUtil;
import org.example.util.SimhashChecker;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@WebServlet("/api/submissions/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 50,       // 50MB
    maxRequestSize = 1024 * 1024 * 100    // 100MB
)
public class SubmissionServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            // 获取表单字段
            String assignmentIdStr = req.getParameter("assignmentId");
            String studentIdStr = req.getParameter("studentId");
            String contentText = req.getParameter("contentText");
            
            if (assignmentIdStr == null || studentIdStr == null) {
                throw new IllegalArgumentException("缺少必要的参数");
            }
            
            int assignmentId = Integer.parseInt(assignmentIdStr);
            int studentId = Integer.parseInt(studentIdStr);

            // 获取上传的文件 Part
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                throw new IllegalArgumentException("未检测到上传的文件");
            }

            String submittedFileName = getFileName(filePart);
            String extension = "";
            int i = submittedFileName.lastIndexOf('.');
            if (i > 0) {
                extension = submittedFileName.substring(i);
            }
            
            // 生成唯一的文件名防止覆盖
            String uniqueFileName = UUID.randomUUID().toString() + extension;
            ObjectStorageService storage = ObjectStorageFactory.get(req.getServletContext());
            String objectKey = storage.newObjectKey(uniqueFileName);

            File tempFile = File.createTempFile("peerreview-upload-", extension);
            try {
                filePart.write(tempFile.getAbsolutePath());
                String contentType = Files.probeContentType(tempFile.toPath());
                if (contentType == null) {
                    contentType = filePart.getContentType();
                }
                if (contentType == null || contentType.isEmpty()) {
                    contentType = "application/octet-stream";
                }

            // 写入数据库
            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                SubmissionMapper submissionMapper = sqlSession.getMapper(SubmissionMapper.class);
                
                // 检查是否已经提交过
                Submission existing = submissionMapper.selectSubmissionByAssignmentAndStudent(assignmentId, studentId);
                if (existing != null) {
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "您已经提交过该作业，无法重复提交");
                } else {
                    storage.put(objectKey, tempFile, contentType);

                    List<Submission> peers = submissionMapper.selectSubmissionsByAssignment(assignmentId);
                    SimhashChecker.Result simResult = SimhashChecker.check(
                            tempFile, submittedFileName, contentText, peers);

                    Submission submission = new Submission();
                    submission.setAssignmentId(assignmentId);
                    submission.setStudentId(studentId);
                    submission.setFileName(submittedFileName);
                    submission.setFilePath(objectKey);
                    submission.setContentHash(simResult.getContentHashHex());
                    submission.setSimilarityPct(simResult.getMaxSimilarityPct());
                    submission.setStatus("submitted");
                    submission.setContentText((contentText != null && !contentText.isEmpty()) ? contentText : null);

                    submissionMapper.insertSubmission(submission);
                    sqlSession.commit();

                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", simResult.buildUserMessage());
                    jsonResponse.add("data", gson.toJsonTree(submission));
                }
            }
            } finally {
                if (tempFile.exists() && !tempFile.delete()) {
                    tempFile.deleteOnExit();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "提交失败：" + e.getMessage());
        }

        out.print(gson.toJson(jsonResponse));
        out.flush();
    }
    
    // GET /api/submissions/download?submissionId=X — 附件下载（绕过 Tomcat 静态目录符号链接限制）
    private void handleDownload(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sidStr = req.getParameter("submissionId");
        if (sidStr == null || sidStr.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "请提供 submissionId");
            return;
        }
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            SubmissionMapper subMapper = sqlSession.getMapper(SubmissionMapper.class);
            Submission sub = subMapper.selectSubmissionById(Integer.parseInt(sidStr));
            if (sub == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "提交不存在");
                return;
            }

            ObjectStorageService storage = ObjectStorageFactory.get(getServletContext());
            StoredObject stored = storage.open(sub.getFilePath());
            boolean inline = "true".equalsIgnoreCase(req.getParameter("inline"));
            String downloadName = sub.getFileName() != null && !sub.getFileName().isEmpty()
                    ? sub.getFileName() : "attachment";

            if (stored != null) {
                try {
                    String contentType = stored.getContentType();
                    if (contentType == null || contentType.isEmpty()) {
                        contentType = "application/octet-stream";
                    }
                    resp.setContentType(contentType);
                    if (stored.getSize() >= 0) {
                        resp.setContentLengthLong(stored.getSize());
                    }
                    setContentDisposition(resp, inline, downloadName);
                    byte[] buffer = new byte[8192];
                    int read;
                    try (OutputStream out = resp.getOutputStream();
                         java.io.InputStream in = stored.getInputStream()) {
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    }
                } finally {
                    stored.close();
                }
                return;
            }

            String contentText = sub.getContentText();
            if (contentText != null && !contentText.isEmpty()) {
                byte[] bytes = contentText.getBytes(StandardCharsets.UTF_8);
                resp.setContentType("text/plain;charset=UTF-8");
                resp.setContentLength(bytes.length);
                setContentDisposition(resp, inline, fallbackTextName(downloadName));
                resp.getOutputStream().write(bytes);
                return;
            }

            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "附件文件不存在");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private static void setContentDisposition(HttpServletResponse resp, boolean inline, String fileName)
            throws IOException {
        String encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        String disposition = (inline ? "inline" : "attachment") + "; filename=\"" + fileName
                + "\"; filename*=UTF-8''" + encoded;
        resp.setHeader("Content-Disposition", disposition);
    }

    private static String fallbackTextName(String originalName) {
        if (originalName == null || originalName.isEmpty()) {
            return "attachment.txt";
        }
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) {
            return originalName.substring(0, dot) + ".txt";
        }
        return originalName + ".txt";
    }

    // GET /api/submissions/preview?submissionId=X — 附件预览
    private void handlePreview(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();
        String sidStr = req.getParameter("submissionId");
        if (sidStr == null || sidStr.isEmpty()) {
            result.addProperty("success", false);
            result.addProperty("message", "请提供 submissionId");
            out.print(gson.toJson(result));
            return;
        }
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            SubmissionMapper subMapper = sqlSession.getMapper(SubmissionMapper.class);
            int sid = Integer.parseInt(sidStr);
            Submission sub = subMapper.selectSubmissionById(sid);
            if (sub == null) {
                result.addProperty("success", false);
                result.addProperty("message", "提交不存在");
                out.print(gson.toJson(result));
                return;
            }
            result.addProperty("success", true);
            result.add("data", AttachmentPreviewUtil.buildPreview(sub, getServletContext()));
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }

    // GET /api/submissions/detail?submissionId=X  — 学生查看自己的提交详情
    private void handleDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();
        String sidStr = req.getParameter("submissionId");
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            SubmissionMapper subMapper = sqlSession.getMapper(SubmissionMapper.class);
            PeerReviewMapper prMapper = sqlSession.getMapper(PeerReviewMapper.class);
            ReviewMapper revMapper = sqlSession.getMapper(ReviewMapper.class);
            AppealMapper appealMapper = sqlSession.getMapper(AppealMapper.class);
            AssignmentMapper asgnMapper = sqlSession.getMapper(AssignmentMapper.class);
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

            int sid = Integer.parseInt(sidStr);
            Submission sub = subMapper.selectSubmissionById(sid);
            if (sub == null) { result.addProperty("success", false); result.addProperty("message", "提交不存在"); out.print(gson.toJson(result)); return; }

            JsonObject data = new JsonObject();
            data.addProperty("submissionId", sub.getSubmissionId());
            data.addProperty("fileName", sub.getFileName());
            data.addProperty("filePath", sub.getFilePath());
            data.addProperty("contentText", sub.getContentText());
            data.addProperty("status", sub.getStatus());
            data.addProperty("finalScore", sub.getFinalScore());
            data.addProperty("finalComment", sub.getFinalComment());

            Assignment asgn = asgnMapper.selectAssignmentById(sub.getAssignmentId());
            if (asgn != null) {
                data.addProperty("assignmentTitle", asgn.getTitle());
                data.addProperty("assignmentStatus", asgn.getStatus());
                data.addProperty("dueDate", asgn.getDueDate() != null ? asgn.getDueDate().toString() : null);
            }

            // 互评结果
            List<PeerReviewAssignment> pras = prMapper.selectByAssignmentId(sub.getAssignmentId());
            JsonArray reviews = new JsonArray();
            for (PeerReviewAssignment pra : pras) {
                if (!pra.getSubmissionId().equals(sid)) continue;
                Review review = revMapper.selectByPraId(pra.getPraId());
                if (review == null) continue;
                JsonObject r = new JsonObject();
                r.addProperty("praId", pra.getPraId());
                r.addProperty("totalScore", review.getTotalScore());
                r.addProperty("overallComment", review.getOverallComment());
                List<java.util.Map<String, Object>> scores = revMapper.selectScoresByReviewId(review.getReviewId());
                JsonArray scoreArr = new JsonArray();
                for (java.util.Map<String, Object> s : scores) {
                    JsonObject sc = new JsonObject();
                    sc.addProperty("itemName", (String) s.get("itemName"));
                    sc.addProperty("score", ((Number) s.get("score")).doubleValue());
                    sc.addProperty("maxScore", ((Number) s.get("maxScore")).doubleValue());
                    scoreArr.add(sc);
                }
                r.add("scores", scoreArr);
                reviews.add(r);
            }
            data.add("peerReviews", reviews);

            // 申诉信息
            List<org.example.entity.Appeal> appeals = appealMapper.selectByStudentId(sub.getStudentId());
            JsonArray appealArr = new JsonArray();
            for (org.example.entity.Appeal a : appeals) {
                if (!a.getSubmissionId().equals(sid)) continue;
                JsonObject ap = new JsonObject();
                ap.addProperty("appealId", a.getAppealId());
                ap.addProperty("reason", a.getReason());
                ap.addProperty("status", a.getStatus());
                ap.addProperty("handlerResponse", a.getHandlerResponse());
                ap.addProperty("adjustedScore", a.getAdjustedScore());
                appealArr.add(ap);
            }
            data.add("appeals", appealArr);

            result.addProperty("success", true);
            result.add("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path != null && path.contains("/download")) {
            handleDownload(req, resp);
            return;
        }
        if (path != null && path.contains("/preview")) {
            handlePreview(req, resp);
            return;
        }
        if (path != null && path.contains("/detail")) {
            handleDetail(req, resp);
            return;
        }
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        String assignmentIdStr = req.getParameter("assignmentId");
        String studentIdStr = req.getParameter("studentId");

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            SubmissionMapper submissionMapper = sqlSession.getMapper(SubmissionMapper.class);
            if (studentIdStr != null && !studentIdStr.isEmpty()) {
                Submission submission = submissionMapper.selectSubmissionByAssignmentAndStudent(
                        Integer.parseInt(assignmentIdStr), Integer.parseInt(studentIdStr));
                jsonResponse.addProperty("success", true);
                jsonResponse.add("data", gson.toJsonTree(submission));
            } else {
                int assignmentId = Integer.parseInt(assignmentIdStr);
                UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
                String pageStr = req.getParameter("page");
                List<Submission> subs;
                if (pageStr != null && !pageStr.isEmpty()) {
                    int page = Math.max(1, Integer.parseInt(pageStr));
                    int pageSize = 10;
                    String pageSizeStr = req.getParameter("pageSize");
                    if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                        pageSize = Math.min(100, Math.max(1, Integer.parseInt(pageSizeStr)));
                    }
                    int total = submissionMapper.countSubmissionsByAssignment(assignmentId);
                    int offset = (page - 1) * pageSize;
                    subs = submissionMapper.selectSubmissionsByAssignmentPaged(assignmentId, offset, pageSize);
                    jsonResponse.addProperty("total", total);
                    jsonResponse.addProperty("page", page);
                    jsonResponse.addProperty("pageSize", pageSize);
                } else {
                    subs = submissionMapper.selectSubmissionsByAssignment(assignmentId);
                }
                JsonArray arr = buildSubmissionList(userMapper, subs);
                jsonResponse.addProperty("success", true);
                jsonResponse.add("data", arr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "获取提交记录失败：" + e.getMessage());
        }
        out.print(gson.toJson(jsonResponse));
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
                SubmissionMapper mapper = sqlSession.getMapper(SubmissionMapper.class);
                mapper.setFinalScore(body.get("submissionId").getAsInt(),
                        body.get("finalScore").getAsDouble(),
                        body.has("finalComment") ? body.get("finalComment").getAsString() : null);
                sqlSession.commit();
                result.addProperty("success", true);
                result.addProperty("message", "最终打分已保存");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return "";
    }

    private JsonArray buildSubmissionList(UserMapper userMapper, List<Submission> subs) {
        JsonArray arr = new JsonArray();
        for (Submission s : subs) {
            User submitter = userMapper.selectUserById(s.getStudentId());
            JsonObject obj = new JsonObject();
            obj.addProperty("submissionId", s.getSubmissionId());
            obj.addProperty("assignmentId", s.getAssignmentId());
            obj.addProperty("studentId", s.getStudentId());
            if (submitter != null) {
                obj.addProperty("studentName", submitter.getRealName());
                obj.addProperty("studentNo", submitter.getUsername());
            }
            obj.addProperty("fileName", s.getFileName());
            obj.addProperty("filePath", s.getFilePath());
            obj.addProperty("contentText", s.getContentText());
            obj.addProperty("status", s.getStatus());
            obj.addProperty("submittedAt", s.getSubmittedAt() != null ? s.getSubmittedAt().toString() : null);
            obj.addProperty("finalScore", s.getFinalScore());
            obj.addProperty("finalComment", s.getFinalComment());
            obj.addProperty("similarityPct", s.getSimilarityPct());
            obj.addProperty("contentHash", s.getContentHash());
            arr.add(obj);
        }
        return arr;
    }
}
