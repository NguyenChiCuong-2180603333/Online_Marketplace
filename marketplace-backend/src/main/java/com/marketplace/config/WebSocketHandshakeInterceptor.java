package com.marketplace.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String uri = request.getURI().toString();
        if (uri.contains("token=")) {
            String token = uri.substring(uri.indexOf("token=") + 6);
            int ampIdx = token.indexOf('&');
            if (ampIdx != -1) token = token.substring(0, ampIdx);
            attributes.put("token", token);
        }
        attributes.put("org.springframework.http.server.ServerHttpRequest.uri", uri);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
    }
} 