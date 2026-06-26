package org.example.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * MinIO S3 兼容对象存储。
 */
public class MinioObjectStorage implements ObjectStorageService {

    private static volatile MinioObjectStorage instance;

    private final MinioClient client;
    private final String bucket;

    private MinioObjectStorage() {
        String endpoint = env("MINIO_ENDPOINT", "http://minio:9000");
        String accessKey = env("MINIO_ACCESS_KEY", "minioadmin");
        String secretKey = env("MINIO_SECRET_KEY", "minioadmin123");
        this.bucket = env("MINIO_BUCKET", "peerreview-uploads");
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        ensureBucket();
    }

    public static MinioObjectStorage getInstance() {
        if (instance == null) {
            synchronized (MinioObjectStorage.class) {
                if (instance == null) {
                    instance = new MinioObjectStorage();
                }
            }
        }
        return instance;
    }

    private void ensureBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new IllegalStateException("MinIO bucket 初始化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void put(String objectKey, File sourceFile, String contentType) throws IOException {
        String key = ObjectStorageService.normalizeKey(objectKey);
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }
        try (InputStream in = new BufferedInputStream(new FileInputStream(sourceFile))) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(in, sourceFile.length(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new IOException("MinIO 上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        try {
            client.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(ObjectStorageService.normalizeKey(objectKey))
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public StoredObject open(String objectKey) throws IOException {
        String key = ObjectStorageService.normalizeKey(objectKey);
        try {
            StatObjectResponse stat = client.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .build());
            InputStream stream = client.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .build());
            String contentType = stat.contentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }
            return new StoredObject(stream, stat.size(), contentType);
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return null;
            }
            throw new IOException("MinIO 读取失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("MinIO 读取失败: " + e.getMessage(), e);
        }
    }

    @Override
    public LocalFileHandle resolveAsLocalFile(String objectKey) throws IOException {
        String key = ObjectStorageService.normalizeKey(objectKey);
        try {
            StatObjectResponse stat = client.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .build());
            String suffix = "";
            int dot = key.lastIndexOf('.');
            if (dot >= 0) {
                suffix = key.substring(dot);
            }
            File temp = File.createTempFile("peerreview-", suffix);
            try (InputStream in = client.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .build())) {
                Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            temp.deleteOnExit();
            return new LocalFileHandle(temp, true);
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return new LocalFileHandle(null, false);
            }
            throw new IOException("MinIO 下载失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IOException("MinIO 下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String objectKey) throws IOException {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(ObjectStorageService.normalizeKey(objectKey))
                    .build());
        } catch (Exception e) {
            throw new IOException("MinIO 删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String newObjectKey(String storedFileName) {
        return "uploads/" + storedFileName;
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
