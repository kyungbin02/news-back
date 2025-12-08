package myapp.backend.domain.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import myapp.backend.domain.auth.mapper.UserMapper;
import myapp.backend.domain.auth.service.JwtService;
import myapp.backend.domain.auth.vo.UserVO;
import myapp.backend.domain.admin.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminService adminService;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect.");
            return;
        }

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId;
        String snsId = null;

        if (attributes.containsKey("response")) {
            Map<String, Object> responseData = (Map<String, Object>) attributes.get("response");
            snsId = (String) responseData.get("id");
            registrationId = "naver";

        } else if (attributes.containsKey("sub")) {
            snsId = (String) attributes.get("sub");
            registrationId = "google";

        } else {
            snsId = String.valueOf(attributes.get("id"));
            registrationId = "kakao";
        }

        if (snsId == null) {
            throw new IllegalStateException("snsId가 할당되지 않았습니다.");
        }

        UserVO user;
        try {
            user = userMapper.findBySnsIdAndSnsType(snsId, registrationId);

            // ★ 추가: 구글 최초 로그인 시 DB에 자동 등록
            if (user == null && "google".equals(registrationId)) {
                user = new UserVO();
                user.setSns_type("google");
                user.setSns_id(snsId);
                user.setUsername((String) attributes.get("name"));
                user.setEmail((String) attributes.get("email"));
                user.setUser_status("active");
                userMapper.insertUser(user);
                logger.info("구글 신규 사용자 등록 완료: snsId=" + snsId);
            }

            logger.info("사용자 조회 성공: snsId=" + snsId + ", registrationId=" + registrationId);

            Integer dbUserId = userMapper.getUserIdBySnsInfo(snsId, registrationId);
            if (dbUserId != null && dbUserId > 0) {
                user.setUser_id(dbUserId);
                logger.info("사용자 ID 설정: " + dbUserId);
            }

            if (user == null || user.getUser_id() == 0) {
                logger.error("로그인 후 사용자 정보를 찾거나 ID를 가져올 수 없습니다. user=" + user);
                getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/login?error=user_processing_failed");
                return;
            }
        } catch (Exception e) {
            logger.error("데이터베이스 연결 또는 사용자 조회 중 오류 발생: " + e.getMessage(), e);
            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/login?error=database_error");
            return;
        }

        // 제재 상태 확인
        String userStatus = user.getUser_status();
        logger.info("사용자 상태 확인: " + userStatus + ", 사용자 ID: " + user.getUser_id());

        if (userStatus != null && !userStatus.equals("active")) {
            if ("suspended".equals(userStatus)) {
                logger.info("정지된 사용자 로그인 시도");
                if (user.getSanction_end_date() != null) {
                    LocalDateTime endDate = LocalDateTime.parse(user.getSanction_end_date(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    if (LocalDateTime.now().isBefore(endDate)) {
                        try {
                            String encodedReason = URLEncoder.encode(user.getSanction_reason(), StandardCharsets.UTF_8);
                            String encodedEndDate = URLEncoder.encode(user.getSanction_end_date(), StandardCharsets.UTF_8);
                            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                                    .queryParam("error", "account_suspended")
                                    .queryParam("reason", encodedReason)
                                    .queryParam("endDate", encodedEndDate)
                                    .build().toUriString();
                            logger.info("정지된 사용자 리다이렉트: " + errorUrl);
                            response.sendRedirect(errorUrl);
                            return;
                        } catch (Exception e) {
                            logger.error("정지 사용자 리다이렉트 실패: " + e.getMessage(), e);
                            response.sendRedirect(frontendUrl + "/login?error=suspended_redirect_failed");
                            return;
                        }
                    } else {
                        userMapper.updateUserStatus(user.getUser_id(), "active", null, null, null);
                        logger.info("사용자 " + user.getUser_id() + "의 정지 기간이 만료되어 상태를 복구했습니다.");
                    }
                }
            } else if ("warning".equals(userStatus)) {
                logger.info("경고 받은 사용자 로그인 시도");
                userMapper.updateUserStatus(user.getUser_id(), "active", null, null, null);
                logger.info("사용자 " + user.getUser_id() + "의 경고를 확인하여 상태를 복구했습니다.");

                try {
                    boolean isAdmin = adminService.isAdmin(user.getUser_id());
                    String jwtToken = jwtService.generateToken(user, isAdmin);
                    String encodedReason = URLEncoder.encode(user.getSanction_reason(), StandardCharsets.UTF_8);
                    String warningUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                            .queryParam("warning", "true")
                            .queryParam("reason", encodedReason)
                            .queryParam("token", jwtToken)
                            .build().toUriString();
                    logger.info("경고 받은 사용자 리다이렉트 (경고 해제됨): " + warningUrl);

                    response.sendRedirect(warningUrl);
                    return;
                } catch (Exception e) {
                    logger.error("경고 사용자 리다이렉트 실패: " + e.getMessage(), e);
                    response.sendRedirect(frontendUrl + "/login?error=warning_redirect_failed");
                    return;
                }
            }
        }

        try {
            boolean isAdmin = adminService.isAdmin(user.getUser_id());
            String jwtToken = jwtService.generateToken(user, isAdmin);
            logger.info("JWT 토큰 생성 성공 (isAdmin: " + isAdmin + ")");

            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/")
                    .queryParam("token", jwtToken)
                    .build().toUriString();

            logger.info("Generated JWT and redirecting to: " + targetUrl);

            clearAuthenticationAttributes(request);
            response.sendRedirect(targetUrl);
        } catch (Exception e) {
            logger.error("JWT 토큰 생성 또는 리다이렉션 중 오류 발생: " + e.getMessage(), e);
            response.sendRedirect(frontendUrl + "/login?error=jwt_generation_failed");
        }
    }
}
