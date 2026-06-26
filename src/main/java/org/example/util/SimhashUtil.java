package org.example.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 64-bit simhash 实现，用于作业文本相似度粗检。
 */
public final class SimhashUtil {

    /** 海明距离 ≤ 该阈值时视为高度相似（与任务书一致） */
    public static final int SIMILAR_HAMMING_THRESHOLD = 3;

    private SimhashUtil() {}

    public static long compute(String text) {
        List<String> tokens = tokenize(text);
        if (tokens.isEmpty()) {
            return 0L;
        }
        int[] vector = new int[64];
        for (String token : tokens) {
            long hash = hash64(token);
            for (int i = 0; i < 64; i++) {
                if (((hash >> i) & 1L) == 1L) {
                    vector[i]++;
                } else {
                    vector[i]--;
                }
            }
        }
        long simhash = 0L;
        for (int i = 0; i < 64; i++) {
            if (vector[i] > 0) {
                simhash |= (1L << i);
            }
        }
        return simhash;
    }

    public static int hammingDistance(long a, long b) {
        return Long.bitCount(a ^ b);
    }

    /** 相似度百分比：0~100，值越大越相似 */
    public static double similarityPercent(long a, long b) {
        int dist = hammingDistance(a, b);
        return (64 - dist) * 100.0 / 64.0;
    }

    public static String toHex(long simhash) {
        return String.format("%016x", simhash);
    }

    public static long fromHex(String hex) {
        if (hex == null || hex.trim().isEmpty()) {
            return 0L;
        }
        return Long.parseUnsignedLong(hex.trim(), 16);
    }

    static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return tokens;
        }
        String normalized = text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fff\\s]", " ")
                .trim();
        if (normalized.isEmpty()) {
            return tokens;
        }

        String[] words = normalized.split("\\s+");
        for (String word : words) {
            if (word.length() >= 2) {
                tokens.add(word);
            }
        }

        String chineseOnly = normalized.replaceAll("[^\\u4e00-\\u9fff]", "");
        for (int i = 0; i < chineseOnly.length() - 1; i++) {
            tokens.add(chineseOnly.substring(i, i + 2));
        }

        if (tokens.isEmpty()) {
            tokens.add(normalized.length() > 200 ? normalized.substring(0, 200) : normalized);
        }
        return tokens;
    }

    private static long hash64(String input) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < input.length(); i++) {
            hash ^= input.charAt(i);
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}
