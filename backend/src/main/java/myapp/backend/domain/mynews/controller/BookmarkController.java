package myapp.backend.domain.mynews.controller;

import myapp.backend.domain.auth.vo.UserPrincipal;
import myapp.backend.domain.mynews.domain.Bookmark;
import myapp.backend.domain.mynews.exception.UnauthorizedException;
import myapp.backend.domain.mynews.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@CrossOrigin(origins = "http://localhost:3000")
public class BookmarkController {
    
    @Autowired
    private BookmarkService bookmarkService;
    
    /**
     * 북마크 목록 조회
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getBookmarks(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            // 인증 검증
            if (userPrincipal == null) {
                throw new UnauthorizedException("로그인이 필요합니다.");
            }
            
            // 인증된 사용자 정보에서 사용자 ID 추출
            Integer userId = userPrincipal.getUserId();
            
            List<Bookmark> bookmarks = bookmarkService.getBookmarksByUserId(userId);
            int totalCount = bookmarkService.getBookmarkCountByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", bookmarks);
            response.put("totalCount", totalCount);
            response.put("message", "북마크 목록을 성공적으로 조회했습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "북마크 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 북마크 추가
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody Map<String, Object> request) {
        try {
            // 인증 검증
            if (userPrincipal == null) {
                throw new UnauthorizedException("로그인이 필요합니다.");
            }
            
            Integer userId = userPrincipal.getUserId();
            Integer newsId = Integer.parseInt(request.get("newsId").toString());
            
            Bookmark bookmark = bookmarkService.addBookmark(userId, newsId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", bookmark);
            response.put("message", "북마크가 성공적으로 추가되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "북마크 추가 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 북마크 삭제 (북마크 ID로)
     */
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Map<String, Object>> deleteBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("bookmarkId") Integer bookmarkId) {
        try {
            // 인증 검증
            if (userPrincipal == null) {
                throw new UnauthorizedException("로그인이 필요합니다.");
            }
            
            Integer userId = userPrincipal.getUserId();
            
            boolean deleted = bookmarkService.deleteBookmark(bookmarkId, userId);
            
            Map<String, Object> response = new HashMap<>();
            if (deleted) {
                response.put("success", true);
                response.put("message", "북마크가 성공적으로 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "북마크를 찾을 수 없거나 삭제할 수 없습니다.");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "북마크 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 북마크 삭제 (뉴스 ID로)
     */
    @DeleteMapping("/news/{newsId}")
    public ResponseEntity<Map<String, Object>> deleteBookmarkByNewsId(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("newsId") Integer newsId) {
        try {
            // 인증 검증
            if (userPrincipal == null) {
                throw new UnauthorizedException("로그인이 필요합니다.");
            }
            
            Integer userId = userPrincipal.getUserId();
            
            boolean deleted = bookmarkService.deleteBookmarkByNewsId(userId, newsId);
            
            Map<String, Object> response = new HashMap<>();
            if (deleted) {
                response.put("success", true);
                response.put("message", "북마크가 성공적으로 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "북마크를 찾을 수 없거나 삭제할 수 없습니다.");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "북마크 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 특정 뉴스의 북마크 여부 확인
     */
    @GetMapping("/check/{newsId}")
    public ResponseEntity<Map<String, Object>> checkBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request) {
        try {
            // URL에서 newsId 추출
            String pathInfo = request.getRequestURI();
            String newsIdStr = pathInfo.substring(pathInfo.lastIndexOf('/') + 1);
            Integer newsId = Integer.parseInt(newsIdStr);
            
            System.out.println("=== 북마크 확인 API 호출 ===");
            System.out.println("newsId: " + newsId + " (타입: " + newsId.getClass().getSimpleName() + ")");
            System.out.println("userPrincipal: " + (userPrincipal != null ? "인증됨" : "null"));
            
            // 인증 검증
            if (userPrincipal == null) {
                System.out.println("인증 실패: userPrincipal이 null");
                throw new UnauthorizedException("로그인이 필요합니다.");
            }
            
            Integer userId = userPrincipal.getUserId();
            System.out.println("userId: " + userId);
            
            Bookmark bookmark = bookmarkService.getBookmarkByUserIdAndNewsId(userId, newsId);
            System.out.println("bookmark 조회 결과: " + (bookmark != null ? "존재함" : "없음"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("isBookmarked", bookmark != null);
            response.put("bookmark", bookmark);
            
            System.out.println("응답 데이터: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("북마크 확인 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "북마크 확인 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
}
