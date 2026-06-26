package org.example.servlet;

import org.apache.ibatis.session.SqlSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.util.MyBatisUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@WebServlet("/api/export")
public class ExportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String assignmentIdStr = req.getParameter("assignmentId");
        if (assignmentIdStr == null) {
            resp.sendError(400, "缺少 assignmentId");
            return;
        }
        int assignmentId = Integer.parseInt(assignmentIdStr);

        try (SqlSession sqlSession = MyBatisUtil.getSqlSession();
             Workbook wb = new XSSFWorkbook()) {

            // 1. 学生成绩单
            Sheet sheet = wb.createSheet("学生成绩单");
            Row header = sheet.createRow(0);
            String[] headers = {"姓名", "学号", "互评次数", "互评均分", "最低分", "最高分", "最终评分"};
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont(); headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i); c.setCellValue(headers[i]); c.setCellStyle(headerStyle);
            }

            List<Map<String, Object>> scores = sqlSession.selectList(
                    "org.example.mapper.StatisticsMapper.studentScoresFull", assignmentId);
            int rowIdx = 1;
            for (Map<String, Object> s : scores) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(str(s.get("realName")));
                r.createCell(1).setCellValue(str(s.get("username")));
                r.createCell(2).setCellValue(num(s.get("reviewCount")));
                r.createCell(3).setCellValue(num(s.get("avgScore")));
                r.createCell(4).setCellValue(num(s.get("minScore")));
                r.createCell(5).setCellValue(num(s.get("maxScore")));
                r.createCell(6).setCellValue(num(s.get("finalScore")));
            }

            // 2. 互评一致性
            Sheet sheet2 = wb.createSheet("互评一致性");
            Row h2 = sheet2.createRow(0);
            String[] h2s = {"被评学生", "评分数", "标准差", "最低分", "最高分", "状态"};
            for (int i = 0; i < h2s.length; i++) {
                Cell c = h2.createCell(i); c.setCellValue(h2s[i]); c.setCellStyle(headerStyle);
            }
            List<Map<String, Object>> anomalies = sqlSession.selectList(
                    "org.example.mapper.StatisticsMapper.reviewConsistency", assignmentId);
            int r2 = 1;
            for (Map<String, Object> a : anomalies) {
                Row r = sheet2.createRow(r2++);
                r.createCell(0).setCellValue(str(a.get("submitter")));
                r.createCell(1).setCellValue(num(a.get("reviewCount")));
                r.createCell(2).setCellValue(num(a.get("scoreStddev")));
                r.createCell(3).setCellValue(num(a.get("minScore")));
                r.createCell(4).setCellValue(num(a.get("maxScore")));
                r.createCell(5).setCellValue(str(a.get("alert")));
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            for (int i = 0; i < h2s.length; i++) sheet2.autoSizeColumn(i);

            resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            resp.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" +
                    URLEncoder.encode("作业" + assignmentId + "_成绩导出.xlsx", "UTF-8"));
            OutputStream os = resp.getOutputStream();
            wb.write(os);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
    }

    private String str(Object o) { return o != null ? o.toString() : ""; }
    private double num(Object o) {
        if (o == null) return 0;
        try { return ((Number) o).doubleValue(); } catch (Exception e) { return 0; }
    }
}
