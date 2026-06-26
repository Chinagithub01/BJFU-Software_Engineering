package org.example.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class RubricValidatorTest {

    @Test
    public void validateCreateItems_acceptsWeightedTotal100() {
        JsonArray items = new JsonArray();
        items.add(item("需求完整性", 25, 1.0));
        items.add(item("非功能需求", 15, 1.0));
        items.add(item("用例图", 20, 1.0));
        items.add(item("排版", 15, 1.0));
        items.add(item("创新性", 25, 1.0));
        Assert.assertNull(RubricValidator.validateCreateItems(items));
    }

    @Test
    public void validateCreateItems_rejectsWeightedTotalNot100() {
        JsonArray items = new JsonArray();
        items.add(item("排版", 15, 0.5));
        items.add(item("其他", 85, 1.0));
        Assert.assertEquals("评分项加权满分之和须为 100，当前为 92.5",
                RubricValidator.validateCreateItems(items));
    }

    private static JsonObject item(String name, double maxScore, double weight) {
        JsonObject o = new JsonObject();
        o.addProperty("itemName", name);
        o.addProperty("maxScore", maxScore);
        o.addProperty("weight", weight);
        return o;
    }
}
