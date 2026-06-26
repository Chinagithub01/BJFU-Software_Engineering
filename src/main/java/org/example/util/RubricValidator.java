package org.example.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.entity.RubricItemDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 量规校验：加权满分之和须为 100；互评分项得分须在 [0, maxScore] 内。
 */
public final class RubricValidator {

    public static final double WEIGHTED_TOTAL_TARGET = 100.0;
    public static final double TOLERANCE = 0.1;

    private RubricValidator() {
    }

    /** 创建量规时校验 items JSON，通过返回 null，失败返回错误信息。 */
    public static String validateCreateItems(JsonArray items) {
        if (items == null || items.size() == 0) {
            return "至少需要一个评分项";
        }
        double weightedSum = 0;
        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            String name = item.has("itemName") ? item.get("itemName").getAsString().trim() : "";
            if (name.isEmpty()) {
                return "评分项名称不能为空";
            }
            if (!item.has("maxScore")) {
                return "请为每项填写满分";
            }
            double maxScore = item.get("maxScore").getAsDouble();
            double weight = item.has("weight") ? item.get("weight").getAsDouble() : 1.0;
            if (maxScore <= 0) {
                return "满分须大于 0";
            }
            if (weight <= 0) {
                return "权重须大于 0";
            }
            weightedSum += maxScore * weight;
        }
        if (Math.abs(weightedSum - WEIGHTED_TOTAL_TARGET) > TOLERANCE) {
            return String.format("评分项加权满分之和须为 100，当前为 %.1f", weightedSum);
        }
        return null;
    }

    /**
     * 互评提交时按量规重算加权总分并校验分项得分。
     * 返回 null 表示失败（错误信息写入 errorOut），成功时返回总分。
     */
    public static Double computeAndValidateReviewTotal(List<RubricItemDTO> rubricItems,
                                                       JsonArray scores,
                                                       StringBuilder errorOut) {
        if (rubricItems == null || rubricItems.isEmpty()) {
            return null;
        }

        Map<Integer, Double> scoreByItem = new HashMap<>();
        if (scores != null) {
            for (int i = 0; i < scores.size(); i++) {
                JsonObject s = scores.get(i).getAsJsonObject();
                int itemId = s.get("rubricItemId").getAsInt();
                if (itemId == 0) {
                    continue;
                }
                scoreByItem.put(itemId, s.get("score").getAsDouble());
            }
        }

        double total = 0;
        for (RubricItemDTO item : rubricItems) {
            int itemId = item.getItemId();
            if (!scoreByItem.containsKey(itemId)) {
                errorOut.append("缺少评分项「").append(item.getItemName()).append("」的得分");
                return null;
            }
            double score = scoreByItem.get(itemId);
            double maxScore = item.getMaxScore() != null ? item.getMaxScore() : 0;
            double weight = item.getWeight() != null ? item.getWeight() : 1.0;
            if (score < 0 || score > maxScore) {
                errorOut.append("「").append(item.getItemName())
                        .append("」得分须在 0 ~ ").append(maxScore).append(" 之间");
                return null;
            }
            total += score * weight;
        }
        return Math.round(total * 10.0) / 10.0;
    }
}
