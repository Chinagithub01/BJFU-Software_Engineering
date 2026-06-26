package org.example.storage;

import javax.servlet.ServletContext;

/**
 * 按环境变量 STORAGE_BACKEND 选择 local 或 minio（Docker 默认 minio）。
 */
public final class ObjectStorageFactory {

    private ObjectStorageFactory() {
    }

    public static ObjectStorageService get(ServletContext servletContext) {
        String backend = System.getenv("STORAGE_BACKEND");
        if (backend == null || backend.trim().isEmpty()) {
            backend = "minio";
        }
        if ("local".equalsIgnoreCase(backend.trim())) {
            return new LocalObjectStorage(servletContext);
        }
        return MinioObjectStorage.getInstance();
    }

    public static boolean isMinioBackend() {
        String backend = System.getenv("STORAGE_BACKEND");
        if (backend == null || backend.trim().isEmpty()) {
            return true;
        }
        return !"local".equalsIgnoreCase(backend.trim());
    }
}
