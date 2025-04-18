package com.zoowayss.requests;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class ProxyConfig {

    private final String host;

    private final int port;

    private final Proxy.Type type;

    private String username;

    private String password;

    public ProxyConfig(String host, int port) {
        this(host, port, Proxy.Type.HTTP);
    }

    public ProxyConfig(String host, int port, Proxy.Type type) {
        this.host = host;
        this.port = port;
        this.type = type;
    }

    public ProxyConfig credentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public Proxy toProxy() {
        return new Proxy(type, new InetSocketAddress(host, port));
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasCredentials() {
        return username != null && password != null;
    }
} 