package myapp.backend.domain.auth.controller;

import myapp.backend.domain.auth.service.UserService;
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
    private UserService userService;

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
    
    // <경빈> 마이페이지용 사용자 정보 조회 API 추가
    @PostMapping("/user/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            System.out.println("=== 마이페이지 사용자 정보 조회 API 호출 ===");
            System.out.println("userPrincipal: " + (userPrincipal != null ? "인증됨" : "null"));
            System.out.println("요청 헤더 확인 필요 - Authorization 헤더가 올바른지 확인");
            
            if (userPrincipal == null) {
                System.out.println("인증 실패: userPrincipal이 null");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            Integer userId = userPrincipal.getUserId();
            System.out.println("userId: " + userId);
            
            // UserService를 통해 DB에서 사용자 정보 조회
            UserVO user = userService.getUserById(userId);
            System.out.println("조회된 사용자 정보: " + user);
            
            if (user == null) {
                System.out.println("사용자 정보 없음: userId=" + userId);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            // 프론트엔드에서 필요한 정보만 구성
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUser_id());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("snsType", user.getSns_type());
            userInfo.put("profileImg", user.getProfile_img());
            userInfo.put("createdAt", user.getCreated_at());
            userInfo.put("success", true);
            
            System.out.println("응답 데이터: " + userInfo);
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            System.err.println("마이페이지 사용자 정보 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
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
