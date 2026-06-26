package org.example.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.ibatis.session.SqlSession;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.util.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        BufferedReader reader = req.getReader();
        JsonObject jsonRequest = gson.fromJson(reader, JsonObject.class);
        String username = jsonRequest.get("username").getAsString();
        String password = jsonRequest.get("password").getAsString();
        String roleStr = jsonRequest.get("role").getAsString();

        JsonObject jsonResponse = new JsonObject();

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            
            // 检查用户名是否已存在
            User existingUser = userMapper.selectUserByUsername(username);
            if (existingUser != null) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "用户名已被注册！");
            } else {
                User newUser = new User();
                newUser.setUsername(username);
                // 实际生产环境中必须哈希加密 (如 BCrypt)，这里演示存明文以匹配登录验证
                newUser.setPasswordHash(password); 
                newUser.setRealName(username); // 默认真名为用户名
                // 假设 2是教师，4是学生
                newUser.setRoleId("teacher".equals(roleStr) ? 2 : 4); 

                userMapper.insertUser(newUser);
                sqlSession.commit(); // 别忘了提交事务

                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "注册成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "服务器异常：" + e.getMessage());
        }

        out.print(gson.toJson(jsonResponse));
        out.flush();
    }
}
