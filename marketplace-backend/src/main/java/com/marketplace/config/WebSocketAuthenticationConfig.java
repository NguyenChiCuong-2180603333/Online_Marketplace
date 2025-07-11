package com.marketplace.config;

import com.marketplace.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.Collections;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthenticationConfig implements ChannelInterceptor {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // Log STOMP command for debugging
        StompCommand command = accessor.getCommand();
        System.out.println("[WebSocket] STOMP Command: " + command);

        String token = accessor.getFirstNativeHeader("Authorization");
        if (token == null) {
            token = accessor.getFirstNativeHeader("token");
            if (token == null) {
                Object sessionToken = accessor.getSessionAttributes() != null ? accessor.getSessionAttributes().get("token") : null;
                if (sessionToken != null) {
                    token = sessionToken.toString();
                }
            }
            if (token == null) {
                // Lấy token từ URI query param (SockJS/WebSocket)
                Object rawUri = accessor.getSessionAttributes() != null ? accessor.getSessionAttributes().get("org.springframework.http.server.ServerHttpRequest.uri") : null;
                if (rawUri != null) {
                    String uri = rawUri.toString();
                    int idx = uri.indexOf("token=");
                    if (idx != -1) {
                        token = uri.substring(idx + 6);
                        int ampIdx = token.indexOf('&');
                        if (ampIdx != -1) token = token.substring(0, ampIdx);
                    }
                }
            }
            if (token != null && !token.startsWith("Bearer ")) {
                token = "Bearer " + token;
            }
        }
        String userId = null;
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                userId = jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                System.out.println("[WebSocket] Invalid token: " + e.getMessage());
                return message;
            }
        }
        // Luôn gán Principal nếu có userId (cho mọi frame)
        if (userId != null) {
            UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            accessor.setUser(principal);
            accessor.setHeader("simpUser", principal); 
            accessor.setHeader("user", principal);     
            System.out.println("[WebSocket] Set Principal userId: " + userId);
        } else if (command == StompCommand.CONNECT) {
            userId = "anonymous";
            accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));
            System.out.println("[WebSocket] Set Principal as anonymous");
        }
        
        return message;
    }

    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(this);
    }
}