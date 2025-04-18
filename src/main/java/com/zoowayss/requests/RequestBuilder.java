package com.zoowayss.requests;

import java.io.IOException;
import java.util.*;

public class RequestBuilder {

    private String url;

    private String method;

    private Map<String, String> headers;

    private Object data;

    private Object json;

    private ProxyConfig proxy;

    private int connectTimeout = 10000; // 默认10秒

    private int readTimeout = 30000;    // 默认30秒

    private boolean streamMode = false;

    public RequestBuilder(String url) {
        this.url = url;
    }

    public RequestBuilder method(String method) {
        this.method = method;
        return this;
    }

    public RequestBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public RequestBuilder data(Object data) {
        this.data = data;
        return this;
    }

    public RequestBuilder json(Object json) {
        this.json = json;
        return this;
    }

    public RequestBuilder proxy(ProxyConfig proxy) {
        this.proxy = proxy;
        return this;
    }

    public RequestBuilder connectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    public RequestBuilder readTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    public RequestBuilder stream() {
        this.streamMode = true;
        return this;
    }

    public Response execute() throws IOException {
        return Requests.request(url, method, headers,
                                Requests.buildRequestBody(data, json),
                                proxy, streamMode, connectTimeout, readTimeout
        );
    }
} 