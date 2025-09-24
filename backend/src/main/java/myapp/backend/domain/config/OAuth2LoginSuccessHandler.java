package myapp.backend.domain.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import myapp.backend.domain.auth.mapper.UserMapper;
import myapp.backend.domain.auth.service.JwtService;
import myapp.backend.domain.auth.vo.UserVO;
import myapp.backend.domain.admin.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AdminService adminService;

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

        UserVO user = userMapper.findBySnsIdAndSnsType(snsId, registrationId);

        Integer dbUserId = userMapper.getUserIdBySnsInfo(snsId, registrationId);
        if (dbUserId != null && dbUserId > 0) {
            user.setUser_id(dbUserId);
        }

        if (user == null || user.getUser_id() == 0) {
             logger.error("로그인 후 사용자 정보를 찾거나 ID를 가져올 수 없습니다.");
             getRedirectStrategy().sendRedirect(request, response, "/login?error=user_processing_failed");
             return;
        }

        try {
            boolean isAdmin = adminService.isAdmin(user.getUser_id());
            String jwtToken = jwtService.generateToken(user, isAdmin);
            logger.info("JWT 토큰 생성 성공 (isAdmin: " + isAdmin + ")");
            
            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/")
                    .queryParam("token", jwtToken)
                    .build().toUriString();
            
            // 디버깅 로그 강화
            logger.info("Generated JWT and redirecting to: " + targetUrl);

            // 강제 리다이렉트
            clearAuthenticationAttributes(request);
            response.sendRedirect(targetUrl);
        } catch (Exception e) {
            logger.error("JWT 토큰 생성 또는 리다이렉션 중 오류 발생: " + e.getMessage(), e);
            response.sendRedirect("http://localhost:3000/login?error=jwt_generation_failed");
        }
    }
}
