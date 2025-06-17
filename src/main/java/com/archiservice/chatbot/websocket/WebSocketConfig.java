package com.archiservice.chatbot.websocket;

import com.archiservice.chatbot.domain.AuthInfo;
import com.archiservice.common.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.unit.DataSize;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final int HEARTBEAT_INTERVAL_MILLIS = (int) TimeUnit.SECONDS.toMillis(10);
    private static final int MESSAGE_SIZE_LIMIT_BYTES = (int) DataSize.ofKilobytes(64).toBytes();
    private static final int SEND_TIME_LIMIT_MILLIS = (int) TimeUnit.SECONDS.toMillis(20);
    private static final int SEND_BUFFER_SIZE_LIMIT_BYTES = (int) DataSize.ofKilobytes(512).toBytes();

    private final JwtUtil jwtTokenProvider;
    private final FilterChannelInterceptor filterChannelInterceptor;

    @Bean
    public DefaultHandshakeHandler handshakeHandler() {
        return new DefaultHandshakeHandler() {
            @Override
            protected Principal determineUser(ServerHttpRequest request,
                                              WebSocketHandler wsHandler,
                                              Map<String, Object> attributes) {
                String token = extractTokenFromRequest(request);
                System.out.println("token : " + token);

                if (token != null) {
                    boolean isValid = jwtTokenProvider.validateToken(token);

                    if (isValid) {
                        Long userId = jwtTokenProvider.extractUserId(token);
                        Long tagCode = jwtTokenProvider.extractTagCode(token);
                        String ageCode = jwtTokenProvider.extractAgeCode(token);
                        AuthInfo authInfo = AuthInfo.of(userId, ageCode, tagCode);
                        return authInfo;
                    }
                }
                return null;
            }
        };
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(filterChannelInterceptor);
    }


    private String extractTokenFromRequest(ServerHttpRequest request) {

        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }

        // 쿼리 파라미터에서 accessToken 찾기 (수정된 부분)
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    String token = param.substring(6);
                    return token;
                }
            }
        }

        return null;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(handshakeHandler())
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue")
                .setTaskScheduler(heartbeatScheduler())
                .setHeartbeatValue(new long[]{HEARTBEAT_INTERVAL_MILLIS, HEARTBEAT_INTERVAL_MILLIS});
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(MESSAGE_SIZE_LIMIT_BYTES)
                .setSendTimeLimit(SEND_TIME_LIMIT_MILLIS)
                .setSendBufferSizeLimit(SEND_BUFFER_SIZE_LIMIT_BYTES);
    }

    private TaskScheduler heartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.setDaemon(true);
        scheduler.initialize();
        return scheduler;
    }
}
