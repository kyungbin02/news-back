package myapp.backend.domain.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import myapp.backend.domain.auth.service.JwtService;
import myapp.backend.domain.auth.vo.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("[JwtAuthenticationFilter] 요청 URI: " + request.getRequestURI());
        System.out.println("[JwtAuthenticationFilter] 요청 메서드: " + request.getMethod());

        // 댓글 API 요청인지 확인
        boolean isCommentRequest = request.getRequestURI().startsWith("/api/board/comment/");
        if (isCommentRequest) {
            System.out.println("[JwtAuthenticationFilter] 🗨️ 댓글 API 요청 감지: " + request.getRequestURI());
        }

        // 알림 API 요청인지 확인
        boolean isNotificationRequest = request.getRequestURI().startsWith("/api/notifications/");
        if (isNotificationRequest) {
            System.out.println("[JwtAuthenticationFilter] 🔔 알림 API 요청 감지: " + request.getRequestURI());
        }

        // 이미지 조회 API는 JWT 인증을 거치지 않음
        if (request.getRequestURI().startsWith("/api/board/image/")) {
            System.out.println("[JwtAuthenticationFilter] 이미지 요청 우회: " + request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromRequest(request);
        if (isCommentRequest) {
            System.out.println("[JwtAuthenticationFilter] 🗨️ 댓글 API - 요청에서 추출한 토큰: " + (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null"));
        } else if (isNotificationRequest) {
            System.out.println("[JwtAuthenticationFilter] 🔔 알림 API - 요청에서 추출한 토큰: " + (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null"));
        } else {
            System.out.println("[JwtAuthenticationFilter] 요청에서 추출한 토큰: " + (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null"));
        }

        if (StringUtils.hasText(token)) {
            boolean valid = jwtService.validateToken(token);
            if (isCommentRequest) {
                System.out.println("[JwtAuthenticationFilter] 🗨️ 댓글 API - 토큰 유효성 검사 결과: " + valid);
            } else if (isNotificationRequest) {
                System.out.println("[JwtAuthenticationFilter] 🔔 알림 API - 토큰 유효성 검사 결과: " + valid);
            } else {
                System.out.println("[JwtAuthenticationFilter] 토큰 유효성 검사 결과: " + valid);
            }

            if (valid) {
                try {
                    String username = jwtService.extractUsername(token);
                    Integer userId = jwtService.extractUserId(token);
                    String snsType = jwtService.extractSnsType(token);
                    String snsId = jwtService.extractSnsId(token);
                    Boolean isAdmin = jwtService.extractIsAdmin(token);

                    if (isCommentRequest) {
                        System.out.println("[JwtAuthenticationFilter] 🗨️ 댓글 API - 토큰에서 추출한 username: " + username);
                        System.out.println("[JwtAuthenticationFilter] 🗨️ 댓글 API - 토큰에서 추출한 userId: " + userId);
                        System.out.println("[JwtAuthenticationFilter] 🗨️ 댓글 API - 토큰에서 추출한 isAdmin: " + isAdmin);
                    } else {
                        System.out.println("[JwtAuthenticationFilter] 토큰에서 추출한 username: " + username);
                        System.out.println("[JwtAuthenticationFilter] 토큰에서 추출한 userId: " + userId);
                        System.out.println("[JwtAuthenticationFilter] 토큰에서 추출한 isAdmin: " + isAdmin);
                    }

                    if (username != null && userId != null) {
                        UserPrincipal userPrincipal = new UserPrincipal(username, userId, snsType, snsId, isAdmin);

                        // ADMIN 권한이 있으면 ROLE_ADMIN 추가
                        List<GrantedAuthority> authorities = Arrays.asList();
                        if (isAdmin != null && isAdmin) {
                            authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        }

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                authorities
                        );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        if (isCommentRequest) {
                            System.out.println("[JwtAuthenticationFilter] 🗨️ 댓글 API - SecurityContext에 인증 정보 설정 완료 ✅");
                        } else {
                            System.out.println("[JwtAuthenticationFilter] SecurityContext에 인증 정보 설정 완료");
                        }
                    }
                } catch (Exception e) {
                    if (isCommentRequest) {
                        System.err.println("[JwtAuthenticationFilter] 🗨️ 댓글 API - JWT 처리 중 예외 발생:");
                    } else {
                        System.err.println("[JwtAuthenticationFilter] JWT 처리 중 예외 발생:");
                    }
                    e.printStackTrace();
                }
            } else {
                if (isCommentRequest) {
                    System.err.println("[JwtAuthenticationFilter] 🗨️ 댓글 API - 토큰이 유효하지 않음 ❌");
                }
            }
        } else {
            if (isCommentRequest) {
                System.err.println("[JwtAuthenticationFilter] 🗨️ 댓글 API - 토큰이 없거나 비어있음 ❌");
            } else {
                System.out.println("[JwtAuthenticationFilter] 토큰이 없거나 비어있음");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            // 중괄호 제거 (프론트엔드에서 잘못 전달된 경우)
            if (token.startsWith("{") && token.endsWith("}")) {
                token = token.substring(1, token.length() - 1);
                System.out.println("[JwtAuthenticationFilter] 중괄호 제거된 토큰: " + token);
            }
            return token;
        }
        return null;
    }
}
