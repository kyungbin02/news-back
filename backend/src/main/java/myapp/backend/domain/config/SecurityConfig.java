package myapp.backend.domain.config;

import myapp.backend.domain.auth.service.CustomOAuth2UserService;
import myapp.backend.domain.auth.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    
    @Autowired
    private OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS 설정 추가
            .csrf(csrf -> csrf.disable()) // API 서버라 CSRF 비활성화
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 세션 생성 안함
            .exceptionHandling(ex -> 
                ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))  // 인증 실패 시 401 반환
            )
            .authorizeHttpRequests(authorize -> authorize
                // favicon.ico 및 기타 브라우저 자동 요청 허용
                .requestMatchers("/favicon.ico", "/robots.txt", "/sitemap.xml").permitAll()
                // 이미지 조회 API는 가장 먼저 허용 (인증 없이) - 최우선 처리
                .requestMatchers("/api/board/image/**").permitAll()
                // 정적 리소스 허용 (index.html 포함)
                .requestMatchers("/upload/**", "/static/**", "/resources/**", "/css/**", "/js/**", "/images/**", "/image/**", "/index.html").permitAll()
                // 리소스 및 OAuth 로그인 관련 경로는 모두 허용
                .requestMatchers("/", "/api/auth/**", "/login/oauth2/**").permitAll()
                
                // <경빈> 뉴스 API는 인증 없이 접근 가능하도록 추가
                .requestMatchers("/api/news/**").permitAll()
                .requestMatchers("/api/search-tracking/**").permitAll() // <경빈> 검색 추적 API 인증 해제
                .requestMatchers("/api/news-click/**").permitAll() // <경빈> 뉴스 클릭 추적 API 인증 해제
                
                // 사용자 상태 조회 API: 로그인 필요
                .requestMatchers("/api/user/status").authenticated()
                
                // <경빈> 댓글 관련 - GET 요청은 인증 없이, 나머지는 인증 필요
                .requestMatchers(HttpMethod.GET, "/api/comments/news/**").permitAll() // <경빈> 댓글 조회는 인증 해제
                .requestMatchers(HttpMethod.GET, "/api/comments/*/replies").permitAll() // <경빈> 대댓글 조회는 인증 해제
                .requestMatchers(HttpMethod.GET, "/api/comments/*/likes").permitAll() // <경빈> 좋아요 수 조회는 인증 해제
                .requestMatchers(HttpMethod.OPTIONS, "/api/comments/**").permitAll() // <경빈> CORS preflight 허용
                .requestMatchers("/api/comments/**").authenticated() // <경빈> 댓글 작성/수정/삭제는 로그인 필요
                
                // 댓글 API: 조회는 허용, 작성/수정/삭제는 인증 필요
                .requestMatchers(HttpMethod.GET, "/api/board/comment/**").permitAll()  // 댓글 조회는 누구나
                .requestMatchers("/api/board/comment/**").authenticated()  // 댓글 작성/수정/삭제는 인증 필요
                
                // <경빈> 마이뉴스 관련 API - 인증 필요
                .requestMatchers("/api/bookmarks/**").authenticated() // <경빈> 북마크 API는 로그인 필요
                .requestMatchers("/api/view-history/**").authenticated() // <경빈> 조회 기록 API는 로그인 필요
                
                // 게시판: 조회는 허용, 작성/수정/삭제는 인증 필요
                .requestMatchers(HttpMethod.GET, "/api/board/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/board/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/board/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/board/**").authenticated()
                
                // 공지사항 API: 조회는 허용, 관리자 기능은 인증 필요
                .requestMatchers(HttpMethod.GET, "/api/notice/**").permitAll()  // 공지사항 조회는 누구나
                .requestMatchers("/api/notice/**").authenticated()  // 공지사항 작성/수정/삭제는 인증 필요
                
                // 문의사항 API: 로그인 필요
                .requestMatchers("/api/inquiry/**").authenticated()  // 문의사항 모든 기능은 로그인 필요
                
                // 신고 API: 로그인 필요
                .requestMatchers("/api/report/**").authenticated()  // 신고 모든 기능은 로그인 필요
                
                // 관리자 API: 인증 필요
                .requestMatchers("/api/admin/**").authenticated()
                
                // 알림 API: 인증 필요
                .requestMatchers("/api/notifications/**").authenticated()
                
                // 그 외 모든 요청 인증 필요
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .failureUrl("/login?error")
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/api/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"로그아웃 성공\", \"isAuthenticated\":false}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",  // 로컬 개발
            "https://www.newsgo.site"  // 프로덕션 프론트엔드
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));  // 모든 헤더 허용
        configuration.setAllowCredentials(true);  // 쿠키, 인증정보 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
