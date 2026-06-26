package org.example.util;

import org.example.entity.Submission;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 提交时计算 simhash，并与同作业已有提交比对。
 */
public final class SimhashChecker {

    public static final class Result {
        private final String contentHashHex;
        private final double maxSimilarityPct;
        private final int closestHamming;
        private final boolean highSimilarity;

        public Result(String contentHashHex, double maxSimilarityPct, int closestHamming, boolean highSimilarity) {
            this.contentHashHex = contentHashHex;
            this.maxSimilarityPct = maxSimilarityPct;
            this.closestHamming = closestHamming;
            this.highSimilarity = highSimilarity;
        }

        public String getContentHashHex() { return contentHashHex; }
        public double getMaxSimilarityPct() { return maxSimilarityPct; }
        public int getClosestHamming() { return closestHamming; }
        public boolean isHighSimilarity() { return highSimilarity; }

        public String buildUserMessage() {
            StringBuilder sb = new StringBuilder("作业提交成功！");
            if (maxSimilarityPct > 0) {
                sb.append(" 文本相似度最高 ")
                        .append(String.format("%.1f", maxSimilarityPct))
                        .append("%");
            }
            if (highSimilarity) {
                sb.append("（海明距离=").append(closestHamming)
                        .append("，已标记供教师参考，不阻断提交）");
            } else {
                sb.append("（simhash 查重完成）");
            }
            return sb.toString();
        }
    }

    private SimhashChecker() {}

    public static Result check(File uploadedFile, String fileName, String contentText,
                             List<Submission> existingSubmissions) throws IOException {
        String extracted = TextExtractor.extract(uploadedFile, fileName);
        StringBuilder corpus = new StringBuilder();
        if (extracted != null && !extracted.isEmpty()) {
            corpus.append(extracted);
        }
        if (contentText != null && !contentText.trim().isEmpty()) {
            if (corpus.length() > 0) {
                corpus.append('\n');
            }
            corpus.append(contentText.trim());
        }

        long simhash = SimhashUtil.compute(corpus.toString());
        String hex = SimhashUtil.toHex(simhash);

        double maxPct = 0.0;
        int closestHamming = 64;
        boolean highSimilarity = false;

        if (existingSubmissions != null) {
            for (Submission other : existingSubmissions) {
                if (other.getContentHash() == null || other.getContentHash().trim().isEmpty()) {
                    continue;
                }
                try {
                    long otherHash = SimhashUtil.fromHex(other.getContentHash());
                    if (otherHash == 0L && simhash == 0L) {
                        continue;
                    }
                    int dist = SimhashUtil.hammingDistance(simhash, otherHash);
                    double pct = SimhashUtil.similarityPercent(simhash, otherHash);
                    if (pct > maxPct) {
                        maxPct = pct;
                    }
                    if (dist < closestHamming) {
                        closestHamming = dist;
                    }
                    if (dist <= SimhashUtil.SIMILAR_HAMMING_THRESHOLD) {
                        highSimilarity = true;
                    }
                } catch (NumberFormatException ignored) {
                    // 忽略历史脏数据
                }
            }
        }

        if (corpus.length() == 0) {
            maxPct = 0.0;
            closestHamming = 64;
            highSimilarity = false;
        }

        return new Result(hex, maxPct, closestHamming, highSimilarity);
    }
}
