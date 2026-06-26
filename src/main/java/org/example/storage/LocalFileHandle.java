package org.example.storage;

import java.io.File;

/**
 * 供文本提取使用的本地文件；MinIO 模式下可能是临时文件。
 */
public class LocalFileHandle implements AutoCloseable {

    private final File file;
    private final boolean temporary;

    public LocalFileHandle(File file, boolean temporary) {
        this.file = file;
        this.temporary = temporary;
    }

    public File getFile() {
        return file;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public boolean exists() {
        return file != null && file.isFile();
    }

    @Override
    public void close() {
        if (temporary && file != null && file.exists()) {
            file.delete();
        }
    }
}
