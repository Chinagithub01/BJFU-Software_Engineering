package org.example.util;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * 统一解析作业附件在磁盘上的存储位置。
 * Docker 下优先使用环境变量 UPLOADS_DIR（如 /data/uploads），本地开发回退到 webapp/uploads。
 */
public final class UploadPathUtil {

    private static final String UPLOAD_DIR_NAME = "uploads";

    private UploadPathUtil() {
    }

    public static File getUploadRoot(ServletContext context) {
        String envDir = System.getenv("UPLOADS_DIR");
        if (envDir != null && !envDir.trim().isEmpty()) {
            File dir = new File(envDir.trim());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        }
        if (context != null) {
            String base = context.getRealPath("");
            if (base != null) {
                File dir = new File(base, UPLOAD_DIR_NAME);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                return dir;
            }
        }
        File fallback = new File(UPLOAD_DIR_NAME);
        if (!fallback.exists()) {
            fallback.mkdirs();
        }
        return fallback;
    }

    public static File resolveStoredFile(ServletContext context, String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }
        String normalized = filePath.replace('\\', '/').replaceFirst("^/+", "");
        if (normalized.startsWith(UPLOAD_DIR_NAME + "/")) {
            normalized = normalized.substring((UPLOAD_DIR_NAME + "/").length());
        }
        if (normalized.isEmpty()) {
            return null;
        }
        File root = getUploadRoot(context);
        return new File(root, normalized.replace('/', File.separatorChar));
    }

    public static String toRelativePath(String storedFileName) {
        return UPLOAD_DIR_NAME + "/" + storedFileName;
    }
}
