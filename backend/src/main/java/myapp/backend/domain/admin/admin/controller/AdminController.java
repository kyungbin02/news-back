package myapp.backend.domain.admin.admin.controller;

import myapp.backend.domain.admin.admin.service.AdminService;
import myapp.backend.domain.admin.admin.vo.AdminVO;
import myapp.backend.domain.auth.vo.UserPrincipal;
import myapp.backend.domain.auth.vo.UserVO;
import myapp.backend.domain.auth.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserMapper userMapper;

    @GetMapping("/info")
    public ResponseEntity<AdminVO> getAdminInfo(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        int userId = principal.getUserId();
        AdminVO info = adminService.getAdminInfo(userId);
        if (info == null) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(info);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAdminRole(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.ok(false);
        }
        boolean isAdmin = adminService.isAdmin(principal.getUserId());
        return ResponseEntity.ok(isAdmin);
    }
    
    // 특정 사용자 상태 조회 (관리자용)
    @GetMapping("/user/{userId}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStatus(@PathVariable int userId, 
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        
        if (!adminService.isAdmin(principal.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 필요합니다."));
        }
        
        try {
            UserVO user = userMapper.getUserStatus(userId);
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
    
    // 전체 사용자 목록 조회 (관리자용)
    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        
        if (!adminService.isAdmin(principal.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 필요합니다."));
        }
        
        try {
            // 파라미터 없이 기본값 사용
            int page = 0;
            int size = 10;
            int offset = page * size;
            
            java.util.List<UserVO> users = userMapper.getAllUsers(offset, size);
            int total = userMapper.getTotalUserCount();
            
            Map<String, Object> result = new HashMap<>();
            result.put("users", users);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) total / size));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "사용자 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
    
    // 제재된 사용자 목록 조회 (관리자용)
    @GetMapping("/users/sanctioned")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getSanctionedUsers(
            @RequestParam(value="page", defaultValue = "0") int page,
            @RequestParam(value="size", defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }
        
        if (!adminService.isAdmin(principal.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 필요합니다."));
        }
        
        try {
            int offset = page * size;
            java.util.List<UserVO> users = userMapper.getSanctionedUsers(offset, size);
            int total = userMapper.getSanctionedUserCount();
            
            Map<String, Object> result = new HashMap<>();
            result.put("users", users);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) total / size));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "제재된 사용자 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }
}


