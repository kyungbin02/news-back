package myapp.backend.domain.auth.controller;

import myapp.backend.domain.auth.vo.UserPrincipal;
import myapp.backend.domain.auth.vo.UserVO;
import myapp.backend.domain.auth.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // JWT, 세션 모두 처리할 수 있도록 개선된 사용자 정보 반환
    @PostMapping("/user")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal Object principal) {
        Map<String, Object> userInfo = new HashMap<>();

        if (principal instanceof UserPrincipal) {
            // JWT 토큰으로 인증된 경우
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            userInfo.put("user_id", userPrincipal.getUserId());
            userInfo.put("username", userPrincipal.getUsername());
            userInfo.put("sns_type", userPrincipal.getSnsType());
            userInfo.put("sns_id", userPrincipal.getSnsId());
            userInfo.put("isAuthenticated", true);
        } else if (principal instanceof OAuth2User) {
            // OAuth2 로그인 직후 세션으로 인증된 경우
            OAuth2User oauth2User = (OAuth2User) principal;
            Map<String, Object> attributes = oauth2User.getAttributes();

            if (attributes.containsKey("response")) { // 네이버
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                userInfo.put("email", response.get("email"));
                userInfo.put("name", response.get("nickname"));
                userInfo.put("profileImg", response.get("profile_image"));
                userInfo.put("provider", "naver");
            } else if (attributes.containsKey("kakao_account")) { // 카카오
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                userInfo.put("email", kakaoAccount.get("email"));
                userInfo.put("name", profile.get("nickname"));
                userInfo.put("profileImg", profile.get("profile_image_url"));
                userInfo.put("provider", "kakao");
            } else { // 구글
                userInfo.put("email", attributes.get("email"));
                userInfo.put("name", attributes.get("name"));
                userInfo.put("profileImg", attributes.get("picture"));
                userInfo.put("provider", "google");
            }
            userInfo.put("username", oauth2User.getAttribute("name"));
            userInfo.put("isAuthenticated", true);
        } else {
            userInfo.put("isAuthenticated", false);
            userInfo.put("message", "User not authenticated.");
        }
        return userInfo;
    }

    // 사용자 상태 조회 API (일반 사용자용)
    @GetMapping("/user/status")
    public ResponseEntity<Map<String, Object>> getUserStatus(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        
        try {
            UserVO user = userMapper.getUserStatus(principal.getUserId());
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "사용자를 찾을 수 없습니다."));
            }
            
            Map<String, Object> statusInfo = new HashMap<>();
            statusInfo.put("user_id", user.getUser_id());
            statusInfo.put("username", user.getUsername());
            statusInfo.put("email", user.getEmail());
            statusInfo.put("user_status", user.getUser_status());
            statusInfo.put("sanction_reason", user.getSanction_reason());
            statusInfo.put("sanction_start_date", user.getSanction_start_date());
            statusInfo.put("sanction_end_date", user.getSanction_end_date());
            statusInfo.put("is_sanctioned", !"active".equals(user.getUser_status()));
            
            // 남은 제재 일수 계산
            if ("suspended".equals(user.getUser_status()) && user.getSanction_end_date() != null) {
                try {
                    java.time.LocalDateTime endDate = java.time.LocalDateTime.parse(user.getSanction_end_date(), 
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDateTime.now(), endDate);
                    statusInfo.put("days_remaining", Math.max(0, daysRemaining));
                } catch (Exception e) {
                    statusInfo.put("days_remaining", 0);
                }
            } else {
                statusInfo.put("days_remaining", 0);
            }
            
            return ResponseEntity.ok(statusInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "사용자 상태 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    // **로그아웃 컨트롤러는 삭제 권장!**
    // 로그아웃은 SecurityConfig의 logout 설정에 맡기세요.
}
