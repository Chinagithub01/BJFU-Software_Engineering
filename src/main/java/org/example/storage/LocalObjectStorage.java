package org.example.storage;

import org.example.util.UploadPathUtil;

import javax.servlet.ServletContext;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * 本地磁盘 / Docker uploads 卷存储。
 */
public class LocalObjectStorage implements ObjectStorageService {

    private final ServletContext servletContext;

    public LocalObjectStorage(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void put(String objectKey, File sourceFile, String contentType) throws IOException {
        String key = ObjectStorageService.normalizeKey(objectKey);
        String fileName = key.substring("uploads/".length());
        File dest = new File(UploadPathUtil.getUploadRoot(servletContext), fileName);
        File parent = dest.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        Files.copy(sourceFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public boolean exists(String objectKey) {
        File file = resolveFile(objectKey);
        return file != null && file.isFile();
    }

    @Override
    public StoredObject open(String objectKey) throws IOException {
        File file = resolveFile(objectKey);
        if (file == null || !file.isFile()) {
            return null;
        }
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return new StoredObject(new BufferedInputStream(new FileInputStream(file)), file.length(), contentType);
    }

    @Override
    public LocalFileHandle resolveAsLocalFile(String objectKey) {
        File file = resolveFile(objectKey);
        return new LocalFileHandle(file, false);
    }

    @Override
    public void delete(String objectKey) throws IOException {
        File file = resolveFile(objectKey);
        if (file != null && file.isFile()) {
            if (!file.delete()) {
                throw new IOException("无法删除文件: " + file.getAbsolutePath());
            }
        }
    }

    @Override
    public String newObjectKey(String storedFileName) {
        return UploadPathUtil.toRelativePath(storedFileName);
    }

    private File resolveFile(String objectKey) {
        return UploadPathUtil.resolveStoredFile(servletContext, ObjectStorageService.normalizeKey(objectKey));
    }
}
