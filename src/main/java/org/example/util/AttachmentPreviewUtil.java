package org.example.util;

import com.google.gson.JsonObject;
import org.example.entity.Submission;
import org.example.storage.LocalFileHandle;
import org.example.storage.ObjectStorageFactory;
import org.example.storage.ObjectStorageService;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 作业附件预览：解析文件类型、提取文本，文件缺失时回退 contentText。
 */
public final class AttachmentPreviewUtil {

    private static final Set<String> CODE_EXT = new HashSet<>(Arrays.asList(
            "java", "py", "js", "ts", "jsx", "tsx", "vue", "c", "cpp", "h", "hpp",
            "cs", "go", "rs", "sql", "xml", "json", "html", "css", "yaml", "yml",
            "properties", "sh", "bat", "ps1"
    ));

    private AttachmentPreviewUtil() {
    }

    public static JsonObject buildPreview(Submission submission, ServletContext context) {
        JsonObject data = new JsonObject();
        if (submission == null) {
            data.addProperty("previewType", "empty");
            data.addProperty("content", "");
            data.addProperty("fileAvailable", false);
            data.addProperty("source", "none");
            data.addProperty("truncated", false);
            return data;
        }

        String fileName = submission.getFileName() != null ? submission.getFileName() : "";
        String filePath = submission.getFilePath() != null ? submission.getFilePath() : "";
        String contentText = submission.getContentText() != null ? submission.getContentText() : "";

        data.addProperty("fileName", fileName);
        data.addProperty("filePath", filePath);
        data.addProperty("submissionId", submission.getSubmissionId());

        String ext = extension(fileName);
        String previewType = resolvePreviewType(ext);
        data.addProperty("previewType", previewType);
        if ("code".equals(previewType)) {
            data.addProperty("language", ext);
        }

        ObjectStorageService storage = ObjectStorageFactory.get(context);
        boolean fileAvailable = false;
        String content = "";
        String source = "none";
        boolean truncated = false;

        try (LocalFileHandle handle = storage.resolveAsLocalFile(filePath)) {
            fileAvailable = handle.exists();
            if (fileAvailable) {
                try {
                    TextExtractor.PreviewResult extracted = TextExtractor.extractForPreview(
                            handle.getFile(), fileName);
                    content = extracted.getText();
                    truncated = extracted.isTruncated();
                    source = "file";
                } catch (IOException e) {
                    content = contentText;
                    source = contentText.isEmpty() ? "none" : "database";
                }
            }
        } catch (IOException e) {
            fileAvailable = false;
        }

        data.addProperty("fileAvailable", fileAvailable);

        if (!fileAvailable && !contentText.isEmpty()) {
            content = contentText.length() > TextExtractor.getMaxPreviewChars()
                    ? contentText.substring(0, TextExtractor.getMaxPreviewChars()) + "\n...(内容已截断)"
                    : contentText;
            truncated = contentText.length() > TextExtractor.getMaxPreviewChars();
            source = "database";
            if ("empty".equals(previewType) || previewType.isEmpty()) {
                previewType = "text";
                data.addProperty("previewType", previewType);
            }
        } else if (!fileAvailable) {
            if ("unsupported".equals(previewType)) {
                source = "none";
            } else if (content.isEmpty()) {
                data.addProperty("previewType", "empty");
                source = "none";
            }
        }

        data.addProperty("content", content);
        data.addProperty("source", source);
        data.addProperty("truncated", truncated);
        return data;
    }

    static String resolvePreviewType(String ext) {
        if (ext.isEmpty()) {
            return "empty";
        }
        switch (ext) {
            case "pdf":
                return "pdf";
            case "docx":
                return "docx";
            case "doc":
            case "zip":
            case "rar":
            case "7z":
                return "unsupported";
            default:
                if (CODE_EXT.contains(ext)) {
                    return "code";
                }
                if ("txt".equals(ext) || "md".equals(ext) || "markdown".equals(ext)) {
                    return "text";
                }
                return "unsupported";
        }
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
}
