package myapp.backend.domain.config;

import myapp.backend.domain.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtService jwtService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        // 쿼리 파라미터에서 토큰 추출
        URI uri = request.getURI();
        String query = uri.getQuery();
        
        if (query != null && query.contains("token=")) {
            String token = null;
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    token = param.substring(6); // "token=" 제거
                    // URL 디코딩
                    token = java.net.URLDecoder.decode(token, "UTF-8");
                    break;
                }
            }
            
            if (token != null) {
                // 토큰 검증
                if (jwtService.validateToken(token)) {
                    // 세션 속성에 토큰 저장 (나중에 사용 가능)
                    attributes.put("token", token);
                    try {
                        Integer userId = jwtService.extractUserId(token);
                        if (userId != null) {
                            attributes.put("userId", userId);
                            System.out.println("[WebSocketHandshakeInterceptor] 토큰 검증 성공 - userId: " + userId);
                        }
                    } catch (Exception e) {
                        System.err.println("[WebSocketHandshakeInterceptor] 사용자 ID 추출 실패: " + e.getMessage());
                    }
                } else {
                    System.err.println("[WebSocketHandshakeInterceptor] 토큰 검증 실패 - 무효한 토큰");
                    // 토큰이 있지만 유효하지 않은 경우 연결 거부
                    return false;
                }
            } else {
                // 토큰이 없는 경우 연결 허용 (선택적 인증)
                System.out.println("[WebSocketHandshakeInterceptor] 토큰이 없음 - 연결 허용 (선택적 인증)");
            }
        }
        
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 완료 후 처리 (필요시)
    }
}