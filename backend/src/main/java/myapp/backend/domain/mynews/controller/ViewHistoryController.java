package myapp.backend.domain.mynews.controller;

import myapp.backend.domain.auth.vo.UserPrincipal;
import myapp.backend.domain.mynews.domain.ViewHistory;
import myapp.backend.domain.mynews.exception.UnauthorizedException;
import myapp.backend.domain.mynews.service.ViewHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/view-history")
@CrossOrigin(origins = "http://localhost:3000")
public class ViewHistoryController {
    
    @Autowired
    private ViewHistoryService viewHistoryService;
    
    /**
     * 조회 기록 조회 (제한된 개수)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getViewHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        try {
            // 인증 검증
            if (userPrincipal == null) {
                throw new UnauthorizedException("로그인이 필요합니다.");
            }
            
            Integer userId = userPrincipal.getUserId();
            
            List<ViewHistory> viewHistory = viewHistoryService.getViewHistoryByUserId(userId, limit);
            int totalCount = viewHistoryService.getViewHistoryCountByUserId(userId);
            Integer totalReadTime = viewHistoryService.getTotalReadTimeByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", viewHistory);
            response.put("totalCount", totalCount);
            response.put("totalReadTime", totalReadTime != null ? totalReadTime : 0);
            response.put("message", "조회 기록을 성공적으로 조회했습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "조회 기록 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 조회 기록 추가
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addViewHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            System.out.println("=== 조회 기록 추가 API 호출 ===");
            System.out.println("request: " + request);
            System.out.println("userPrincipal: " + (userPrincipal != null ? "인증됨" : "null"));
            
            // 인증 검증
            if (userPrincipal == null) {
                System.out.println("인증 실패: userPrincipal이 null");
                throw new UnauthorizedException("로그인이 필요합니다.");
            }
            
            Integer userId = userPrincipal.getUserId();
            Integer newsId = Integer.parseInt(request.get("newsId").toString());
            Integer readTime = request.get("readTime") != null ? 
                Integer.parseInt(request.get("readTime").toString()) : 0;
            
            System.out.println("userId: " + userId + ", newsId: " + newsId + ", readTime: " + readTime);
            
            ViewHistory viewHistory;
            if (readTime > 0) {
                viewHistory = viewHistoryService.addViewHistory(userId, newsId, readTime);
            } else {
                viewHistory = viewHistoryService.addViewHistory(userId, newsId);
            }
            
            System.out.println("조회 기록 추가 성공: " + viewHistory);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", viewHistory);
            response.put("message", "조회 기록이 성공적으로 추가되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("조회 기록 추가 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "조회 기록 추가 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 읽은 시간 업데이트
     */
    @PutMapping("/{viewId}/read-time")
    public ResponseEntity<Map<String, Object>> updateReadTime(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("viewId") Integer viewId,
            @RequestBody Map<String, Object> request) {
        try {
            // 인증 검증
            if (userPrincipal == null) {
                throw new UnauthorizedException("로그인이 필요합니다.");
            }
            
            Integer userId = userPrincipal.getUserId();
            Integer readTime = Integer.parseInt(request.get("readTime").toString());
            
            boolean updated = viewHistoryService.updateReadTime(viewId, userId, readTime);
            
            Map<String, Object> response = new HashMap<>();
            if (updated) {
                response.put("success", true);
                response.put("message", "읽은 시간이 성공적으로 업데이트되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "조회 기록을 찾을 수 없거나 업데이트할 수 없습니다.");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "읽은 시간 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 사용자 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            // 인증 검증
            if (userPrincipal == null) {
                throw new UnauthorizedException("로그인이 필요합니다.");
            }
            
            Integer userId = userPrincipal.getUserId();
            
            int totalViews = viewHistoryService.getViewHistoryCountByUserId(userId);
            Integer totalReadTime = viewHistoryService.getTotalReadTimeByUserId(userId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalViews", totalViews);
            stats.put("totalReadTime", totalReadTime != null ? totalReadTime : 0);
            stats.put("averageReadTime", totalViews > 0 ? (totalReadTime != null ? totalReadTime / totalViews : 0) : 0);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "사용자 통계를 성공적으로 조회했습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "사용자 통계 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
}
