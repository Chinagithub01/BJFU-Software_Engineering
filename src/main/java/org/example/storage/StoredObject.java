package org.example.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * 已存储对象的只读访问句柄。
 */
public class StoredObject implements AutoCloseable {

    private final InputStream inputStream;
    private final long size;
    private final String contentType;

    public StoredObject(InputStream inputStream, long size, String contentType) {
        this.inputStream = inputStream;
        this.size = size;
        this.contentType = contentType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public long getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
