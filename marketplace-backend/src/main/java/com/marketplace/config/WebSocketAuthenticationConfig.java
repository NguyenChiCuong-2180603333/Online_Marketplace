package com.marketplace.config;

import com.marketplace.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthenticationConfig implements ChannelInterceptor {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authToken = accessor.getFirstNativeHeader("Authorization");

            if (StringUtils.hasText(authToken) && authToken.startsWith("Bearer ")) {
                String token = authToken.substring(7);

                if (jwtTokenProvider.validateToken(token)) {
                    String userId = jwtTokenProvider.getUserIdFromToken(token);

                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );

                    accessor.setUser(authentication);
                }
            }
        }

        return message;
    }

    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(this);
    }
}