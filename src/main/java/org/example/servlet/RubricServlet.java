package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.session.SqlSession;
import org.example.mapper.RubricMapper;
import org.example.util.MyBatisUtil;
import org.example.util.RubricValidator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

@WebServlet("/api/rubrics")
public class RubricServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        JsonObject result = new JsonObject();

        try {
            JsonObject body = gson.fromJson(new InputStreamReader(req.getInputStream(), "UTF-8"), JsonObject.class);
            int assignmentId = body.get("assignmentId").getAsInt();
            JsonArray items = body.getAsJsonArray("items");

            String validationError = RubricValidator.validateCreateItems(items);
            if (validationError != null) {
                result.addProperty("success", false);
                result.addProperty("message", validationError);
                out.print(gson.toJson(result));
                out.flush();
                return;
            }

            try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
                RubricMapper mapper = sqlSession.getMapper(RubricMapper.class);
                Map<String, Object> rubricParams = new HashMap<>();
                rubricParams.put("assignmentId", assignmentId);
                mapper.insertRubric(rubricParams);

                Number rubricIdNum = (Number) rubricParams.get("rubricId");
                if (rubricIdNum == null) {
                    throw new IllegalStateException("无法获取 rubricId");
                }
                int rubricId = rubricIdNum.intValue();

                for (int i = 0; i < items.size(); i++) {
                    JsonObject item = items.get(i).getAsJsonObject();
                    mapper.insertRubricItem(rubricId,
                            item.get("itemName").getAsString(),
                            item.get("maxScore").getAsDouble(),
                            item.has("weight") ? item.get("weight").getAsDouble() : 1.0,
                            item.has("description") ? item.get("description").getAsString() : "",
                            i + 1);
                }

                sqlSession.commit();
                result.addProperty("success", true);
                result.addProperty("message", "评分量规保存成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", "保存失败：" + e.getMessage());
        }
        out.print(gson.toJson(result));
        out.flush();
    }
}
