package com.zoowayss.requests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.*;
import java.util.*;
import java.util.function.*;

public class Response implements Closeable {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private final int statusCode;

    private final String text;

    private final Map<String, String> headers;

    private final InputStream inputStream;

    private boolean streamMode;

    public Response(int statusCode, String text, Map<String, String> headers) {
        this(statusCode, text, headers, null);
    }

    public Response(int statusCode, String text, Map<String, String> headers, InputStream inputStream) {
        this.statusCode = statusCode;
        this.text = text;
        this.headers = headers;
        this.inputStream = inputStream;
        this.streamMode = inputStream != null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getText() {
        return text;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 将响应解析为指定类型的对象
     */
    public <T> T getJson(Class<T> clazz) throws IOException {
        return objectMapper.readValue(text, clazz);
    }

    /**
     * 将响应解析为Map类型
     */
    public Map<String, Object> getJson() throws IOException {
        return objectMapper.readValue(text, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 将响应解析为List类型
     */
    public <T> List<T> jsonList(Class<T> elementClass) throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, elementClass);
        return objectMapper.readValue(text, type);
    }

    /**
     * 将响应解析为复杂的泛型类型
     */
    public <T> T getJson(TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(text, typeReference);
    }

    /**
     * 检查响应是否包含内容
     */
    public boolean hasContent() {
        return text != null && !text.isEmpty();
    }

    /**
     * 检查响应状态是否成功
     */
    public boolean isOk() {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * 以流式方式处理响应内容
     *
     * @param consumer 处理每一块数据的消费者
     */
    public void stream(Consumer<byte[]> consumer) throws IOException {
        if (!streamMode || inputStream == null) {
            throw new IllegalStateException("Response is not in stream mode");
        }

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] data = new byte[bytesRead];
            System.arraycopy(buffer, 0, data, 0, bytesRead);
            consumer.accept(data);
        }
    }

    /**
     * 以流式方式处理响应内容，按行处理
     *
     * @param consumer 处理每一行数据的消费者
     */
    public void streamLines(Consumer<String> consumer) throws IOException {
        if (!streamMode || inputStream == null) {
            throw new IllegalStateException("Response is not in stream mode");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                consumer.accept(line);
            }
        }
    }

    /**
     * 将响应内容保存到文件
     *
     * @param filePath 文件路径
     */
    public void saveToFile(String filePath) throws IOException {
        if (!streamMode || inputStream == null) {
            throw new IllegalStateException("Response is not in stream mode");
        }

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * 获取原始输入流
     * 注意：使用此方法后需要手动关闭流
     */
    public InputStream getInputStream() {
        if (!streamMode || inputStream == null) {
            throw new IllegalStateException("Response is not in stream mode");
        }
        return inputStream;
    }

    /**
     * 检查是否为流式模式
     */
    public boolean isStreamMode() {
        return streamMode;
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }

    @Override
    public String toString() {
        return String.format("<Response [%d]>", statusCode);
    }
} 