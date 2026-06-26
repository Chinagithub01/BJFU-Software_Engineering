package org.example.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 从上传文件中提取纯文本，供 simhash 计算使用。
 */
public final class TextExtractor {

    private static final Set<String> PLAIN_EXT = new HashSet<>(Arrays.asList(
            "txt", "md", "markdown", "java", "py", "js", "ts", "jsx", "tsx", "vue",
            "c", "cpp", "h", "hpp", "cs", "go", "rs", "sql", "xml", "json", "html", "css",
            "yaml", "yml", "properties", "sh", "bat", "ps1"
    ));

    private static final int MAX_CHARS = 512 * 1024;

    private TextExtractor() {}

    public static int getMaxPreviewChars() {
        return MAX_CHARS;
    }

    /** 预览用提取：保留换行，仅做长度截断。 */
    public static PreviewResult extractForPreview(File file, String originalFileName) throws IOException {
        String ext = extension(originalFileName);
        String text;
        switch (ext) {
            case "pdf":
                text = extractPdf(file);
                break;
            case "docx":
                text = extractDocx(file);
                break;
            default:
                if (PLAIN_EXT.contains(ext)) {
                    text = readPlainText(file);
                } else {
                    text = "";
                }
                break;
        }
        return truncateForPreview(text);
    }

    public static final class PreviewResult {
        private final String text;
        private final boolean truncated;

        PreviewResult(String text, boolean truncated) {
            this.text = text;
            this.truncated = truncated;
        }

        public String getText() {
            return text;
        }

        public boolean isTruncated() {
            return truncated;
        }
    }

    private static PreviewResult truncateForPreview(String text) {
        if (text == null) {
            return new PreviewResult("", false);
        }
        if (text.length() <= MAX_CHARS) {
            return new PreviewResult(text, false);
        }
        return new PreviewResult(text.substring(0, MAX_CHARS) + "\n...(内容已截断)", true);
    }

    public static String extract(File file, String originalFileName) throws IOException {
        String ext = extension(originalFileName);
        String text;
        switch (ext) {
            case "pdf":
                text = extractPdf(file);
                break;
            case "docx":
                text = extractDocx(file);
                break;
            default:
                if (PLAIN_EXT.contains(ext)) {
                    text = readPlainText(file);
                } else {
                    text = "";
                }
                break;
        }
        return truncate(text);
    }

    private static String extractPdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    private static String extractDocx(File file) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file));
             XWPFDocument doc = new XWPFDocument(in);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private static String readPlainText(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        String text = new String(bytes, StandardCharsets.UTF_8);
        if (text.contains("\u0000")) {
            text = new String(bytes, StandardCharsets.ISO_8859_1);
        }
        return text;
    }

    private static String extension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.replaceAll("\\s+", " ").trim();
        if (trimmed.length() <= MAX_CHARS) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_CHARS);
    }
}
