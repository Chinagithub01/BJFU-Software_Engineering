package org.example.storage;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;

/**
 * 作业附件对象存储抽象（local 卷 / MinIO）。
 */
public interface ObjectStorageService {

    /** 写入对象，objectKey 形如 uploads/{uuid}.pdf */
    void put(String objectKey, File sourceFile, String contentType) throws IOException;

    boolean exists(String objectKey);

    StoredObject open(String objectKey) throws IOException;

    /** 解析为本地 File，供 PDF/查重等需要随机访问的场景使用 */
    LocalFileHandle resolveAsLocalFile(String objectKey) throws IOException;

    void delete(String objectKey) throws IOException;

    /** 生成新对象的相对 key */
    String newObjectKey(String storedFileName);

    static String normalizeKey(String objectKey) {
        if (objectKey == null) {
            return "";
        }
        String normalized = objectKey.replace('\\', '/').replaceFirst("^/+", "");
        if (normalized.startsWith("uploads/")) {
            return normalized;
        }
        if (normalized.startsWith("uploads")) {
            return "uploads/" + normalized.substring("uploads".length()).replaceFirst("^/+", "");
        }
        return "uploads/" + normalized;
    }
}
