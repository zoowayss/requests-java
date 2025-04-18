package com.zoowayss.requests;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;

public class Requests {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // GET 请求
    public static Response get(String url) throws IOException {
        return builder(url).method("GET").execute();
    }

    // 构建器方法
    public static RequestBuilder builder(String url) {
        return new RequestBuilder(url);
    }

    public static Response get(String url, Map<String, String> headers) throws IOException {
        return builder(url).method("GET").headers(headers).execute();
    }

    public static Response get(String url, Map<String, String> headers, ProxyConfig proxy) throws IOException {
        return builder(url).method("GET").headers(headers).proxy(proxy).execute();
    }

    public static Response get(String url, Map<String, String> headers, int connectTimeout, int readTimeout) throws IOException {
        return builder(url)
                .method("GET")
                .headers(headers)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .execute();
    }

    // 流式GET请求
    public static Response getStream(String url) throws IOException {
        return builder(url).method("GET").stream().execute();
    }

    public static Response getStream(String url, Map<String, String> headers) throws IOException {
        return builder(url).method("GET").headers(headers).stream().execute();
    }

    public static Response getStream(String url, Map<String, String> headers, ProxyConfig proxy) throws IOException {
        return builder(url).method("GET").headers(headers).proxy(proxy).stream().execute();
    }

    public static Response getStream(String url, Map<String, String> headers, int connectTimeout, int readTimeout) throws IOException {
        return builder(url)
                .method("GET")
                .headers(headers)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .stream()
                .execute();
    }

    // POST 请求
    public static Response post(String url, Object data) throws IOException {
        return builder(url).method("POST").data(data).execute();
    }

    public static Response post(String url, Map<String, String> headers) throws IOException {
        return builder(url).method("POST").headers(headers).execute();
    }

    public static Response post(String url, Map<String, String> headers, Object data, Object json) throws IOException {
        return builder(url).method("POST").headers(headers).data(data).json(json).execute();
    }

    public static Response post(String url, Map<String, String> headers, Object data, Object json, ProxyConfig proxy) throws IOException {
        return builder(url)
                .method("POST")
                .headers(headers)
                .data(data)
                .json(json)
                .proxy(proxy)
                .execute();
    }

    public static Response post(String url, Map<String, String> headers, Object data, Object json, int connectTimeout, int readTimeout) throws IOException {
        return builder(url)
                .method("POST")
                .headers(headers)
                .data(data)
                .json(json)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .execute();
    }

    // PUT 请求
    public static Response put(String url, Object data) throws IOException {
        return builder(url).method("PUT").data(data).execute();
    }

    public static Response put(String url, Map<String, String> headers, Object data, Object json) throws IOException {
        return builder(url).method("PUT").headers(headers).data(data).json(json).execute();
    }

    public static Response put(String url, Map<String, String> headers, Object data, Object json, ProxyConfig proxy) throws IOException {
        return builder(url)
                .method("PUT")
                .headers(headers)
                .data(data)
                .json(json)
                .proxy(proxy)
                .execute();
    }

    // DELETE 请求
    public static Response delete(String url) throws IOException {
        return builder(url).method("DELETE").execute();
    }

    public static Response delete(String url, Map<String, String> headers) throws IOException {
        return builder(url).method("DELETE").headers(headers).execute();
    }

    public static Response delete(String url, Map<String, String> headers, ProxyConfig proxy) throws IOException {
        return builder(url).method("DELETE").headers(headers).proxy(proxy).execute();
    }

    public static RequestBody buildRequestBody(Object data, Object json) throws IOException {
        if (json != null) {
            return new RequestBody(objectMapper.writeValueAsString(json), "application/json");
        }
        if (data != null) {
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) data;
                String formData = dataMap.entrySet().stream()
                                         .map(e -> e.getKey() + "=" + e.getValue())
                                         .collect(Collectors.joining("&"));
                return new RequestBody(formData, "application/x-www-form-urlencoded");
            } else {
                return new RequestBody(objectMapper.writeValueAsString(data), "application/json");
            }
        }
        return null;
    }

    static Response request(String url, String method, Map<String, String> headers, RequestBody body, ProxyConfig proxyConfig, boolean streamMode, int connectTimeout, int readTimeout) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection conn;

        if (proxyConfig != null) {
            conn = (HttpURLConnection) urlObj.openConnection(proxyConfig.toProxy());
            if (proxyConfig.hasCredentials()) {
                String auth = proxyConfig.getUsername() + ":" + proxyConfig.getPassword();
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                conn.setRequestProperty("Proxy-Authorization", "Basic " + encodedAuth);
            }
        } else {
            conn = (HttpURLConnection) urlObj.openConnection();
        }

        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setRequestMethod(method);

        // 设置默认请求头
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("User-Agent", "Java-Requests/1.0.0");

        // 设置自定义请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        // 设置请求体
        if (body != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", body.contentType);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.content.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        // 获取响应
        int statusCode = conn.getResponseCode();
        InputStream inputStream = statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream();

        Map<String, String> responseHeaders = new HashMap<>();
        conn.getHeaderFields().forEach((key, value) -> {
            if (key != null) {
                responseHeaders.put(key, String.join(", ", value));
            }
        });

        if (streamMode) {
            return new Response(statusCode, null, responseHeaders, inputStream);
        } else {
            String responseBody = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.joining("\n"));
            inputStream.close();
            conn.disconnect();
            return new Response(statusCode, responseBody, responseHeaders);
        }
    }

    static class RequestBody {

        String content;

        String contentType;

        RequestBody(String content, String contentType) {
            this.content = content;
            this.contentType = contentType;
        }
    }
} 