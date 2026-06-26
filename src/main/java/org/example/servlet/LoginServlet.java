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

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // 读取前端发送的 JSON 数据
        BufferedReader reader = req.getReader();
        JsonObject jsonRequest = gson.fromJson(reader, JsonObject.class);
        String username = jsonRequest.get("username").getAsString();
        String password = jsonRequest.get("password").getAsString();

        JsonObject jsonResponse = new JsonObject();

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession()) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            User user = userMapper.selectUserByUsername(username);

            // 验证用户名和密码
            if (user != null && user.getPasswordHash().equals(password)) {
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "登录成功");
                
                // 返回用户信息给前端 (判断 roleId: 假设 2 是教师，4 是学生，具体视 init.sql 而定)
                // 为了演示，根据 roleId 判断角色字符串
                String roleStr;
                switch (user.getRoleId()) {
                    case 1: roleStr = "admin"; break;
                    case 2: roleStr = "teacher"; break;
                    case 3: roleStr = "ta"; break;
                    default: roleStr = "student"; break;
                }
                
                JsonObject userData = new JsonObject();
                userData.addProperty("userId", user.getUserId());
                userData.addProperty("username", user.getUsername());
                userData.addProperty("realName", user.getRealName());
                userData.addProperty("role", roleStr);
                userData.addProperty("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
                userData.addProperty("email", user.getEmail());

                String token = org.example.util.JwtUtil.generateToken(
                        user.getUserId(), user.getUsername(), roleStr);

                JsonObject data = new JsonObject();
                data.addProperty("token", token);
                data.add("user", userData);
                jsonResponse.add("data", data);
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "用户名或密码错误！");
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
