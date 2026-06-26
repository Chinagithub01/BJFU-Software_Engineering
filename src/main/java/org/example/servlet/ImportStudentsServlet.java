package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import org.apache.ibatis.session.SqlSession;
import org.example.mapper.EnrollmentMapper;
import org.example.mapper.UserMapper;
import org.example.entity.User;
import org.example.util.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/enrollments/import")
public class ImportStudentsServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        try {
            JsonObject body = gson.fromJson(new InputStreamReader(req.getInputStream(), "UTF-8"), JsonObject.class);
            int courseId = body.get("courseId").getAsInt();
            JsonArray usernames = body.getAsJsonArray("usernames");

            int success = 0, skipped = 0;
            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
                EnrollmentMapper enrollMapper = sqlSession.getMapper(EnrollmentMapper.class);

                for (int i = 0; i < usernames.size(); i++) {
                    String username = usernames.get(i).getAsString().trim();
                    if (username.isEmpty()) continue;
                    User user = userMapper.selectUserByUsername(username);
                    if (user == null) { skipped++; continue; }
                    int exists = enrollMapper.checkEnrollmentExists(courseId, user.getUserId());
                    if (exists > 0) { skipped++; continue; }
                    enrollMapper.insertEnrollment(courseId, user.getUserId(), "student");
                    success++;
                }
                sqlSession.commit();
            }

            result.addProperty("success", true);
            result.addProperty("message", "导入完成：成功 " + success + " 人，跳过 " + skipped + " 人");
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", "导入失败：" + e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }
}
