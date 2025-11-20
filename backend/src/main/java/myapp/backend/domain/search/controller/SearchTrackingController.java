package myapp.backend.domain.search.controller;

import myapp.backend.domain.search.domain.SearchKeyword;
import myapp.backend.domain.search.service.SearchTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search-tracking")
// CORS는 SecurityConfig에서 전역으로 설정되어 있으므로 @CrossOrigin 제거
public class SearchTrackingController {

    @Autowired
    private SearchTrackingService searchTrackingService;

    /**
     * 검색어 추적 (검색할 때마다 호출)
     * POST /api/search-tracking
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> trackSearch(@RequestBody Map<String, String> request) {
        try {
            String keyword = request.get("keyword");
            
            if (keyword == null || keyword.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "검색어가 비어있습니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            searchTrackingService.trackSearch(keyword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "검색어가 추적되었습니다.");
            response.put("keyword", keyword);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "검색어 추적 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 인기 검색어 조회 (실시간 검색어)
     * GET /api/search-tracking/top?limit=10
     */
    @GetMapping("/top")
    public ResponseEntity<Map<String, Object>> getTopSearchKeywords(HttpServletRequest request) {
        String limitStr = request.getParameter("limit");
        int limit = limitStr != null ? Integer.parseInt(limitStr) : 10;
        try {
            List<SearchKeyword> topKeywords = searchTrackingService.getTopSearchKeywords(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", topKeywords);
            response.put("limit", limit);
            response.put("total", topKeywords.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "인기 검색어 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 최근 검색어 조회
     * GET /api/search-tracking/recent?limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentSearchKeywords(HttpServletRequest request) {
        String limitStr = request.getParameter("limit");
        int limit = limitStr != null ? Integer.parseInt(limitStr) : 10;
        try {
            List<SearchKeyword> recentKeywords = searchTrackingService.getRecentSearchKeywords(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", recentKeywords);
            response.put("limit", limit);
            response.put("total", recentKeywords.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "최근 검색어 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 전체 검색어 목록 조회 (페이징)
     * GET /api/search-tracking?page=1&size=20
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSearchKeywords(HttpServletRequest request) {
        String pageStr = request.getParameter("page");
        String sizeStr = request.getParameter("size");
        int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
        int size = sizeStr != null ? Integer.parseInt(sizeStr) : 20;
        try {
            List<SearchKeyword> keywords = searchTrackingService.getAllSearchKeywords(page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", keywords);
            response.put("page", page);
            response.put("size", size);
            response.put("total", keywords.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "검색어 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 특정 검색어 정보 조회
     * GET /api/search-tracking/keyword/{keyword}
     */
    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<Map<String, Object>> getSearchKeywordInfo(@PathVariable("keyword") String keyword) {
        try {
            SearchKeyword keywordInfo = searchTrackingService.getSearchKeywordInfo(keyword);
            
            Map<String, Object> response = new HashMap<>();
            if (keywordInfo != null) {
                response.put("success", true);
                response.put("data", keywordInfo);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "검색어 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "검색어 정보 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 검색어 정리 (관리자용)
     * DELETE /api/search-tracking/cleanup
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupSearchKeywords() {
        try {
            searchTrackingService.cleanupOldSearchKeywords();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "검색어 정리가 완료되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "검색어 정리 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}


