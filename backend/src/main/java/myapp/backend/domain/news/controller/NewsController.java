package myapp.backend.domain.news.controller;

import myapp.backend.domain.news.domain.News;
import myapp.backend.domain.news.service.RssService;
import myapp.backend.domain.news.mapper.NewsMapper;
import myapp.backend.domain.search.service.SearchTrackingService;
import myapp.backend.domain.search.service.NewsClickTrackingService;
import myapp.backend.domain.search.domain.NewsClickTracking;
import myapp.backend.domain.mynews.service.ViewHistoryService;
import myapp.backend.domain.auth.vo.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
// CORS는 SecurityConfig에서 전역으로 설정되어 있으므로 @CrossOrigin 제거
public class NewsController {

    @Autowired
    private RssService rssService;
    
    
    @Autowired
    private NewsMapper newsMapper;
    
    @Autowired
    private SearchTrackingService searchTrackingService;
    
    @Autowired
    private NewsClickTrackingService newsClickTrackingService;
    
    @Autowired
    private ViewHistoryService viewHistoryService;

    // <경빈> RSS 뉴스 저장 API
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveRssNews(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== RSS 뉴스 저장 API 호출 ===");
            System.out.println("요청 데이터: " + request);

            // 필수 필드 검증
            if (request.get("newsId") == null || request.get("title") == null || request.get("content") == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "필수 필드가 누락되었습니다. (newsId, title, content 필요)");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Integer newsId = Integer.valueOf(request.get("newsId").toString());
            String title = request.get("title").toString();
            String content = request.get("content").toString();
            String category = request.get("category") != null ? request.get("category").toString() : "general";
            String source = request.get("source") != null ? request.get("source").toString() : null;
            String url = request.get("url") != null ? request.get("url").toString() : null;
            String imageUrl = request.get("imageUrl") != null ? request.get("imageUrl").toString() : null;

            // 중복 체크
            int existsCount = newsMapper.existsByNewsId(newsId);
            if (existsCount > 0) {
                System.out.println("중복 뉴스 발견: newsId=" + newsId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "이미 존재하는 뉴스입니다.");
                response.put("data", Map.of("newsId", newsId, "exists", true));
                return ResponseEntity.status(409).body(response);
            }

            // News 객체 생성
            News news = new News();
            news.setNewsId(newsId);
            news.setTitle(title);
            news.setContent(content);
            news.setCategory(category);
            news.setSource(source);
            news.setUrl(url);
            news.setImageUrl(imageUrl);
            news.setViews(0);
            news.setCreatedAt(LocalDateTime.now());

            // publishedAt 처리
            if (request.get("publishedAt") != null) {
                try {
                    LocalDateTime publishedAt = LocalDateTime.parse(request.get("publishedAt").toString());
                    news.setPublishedAt(publishedAt);
                } catch (Exception e) {
                    System.out.println("publishedAt 파싱 오류: " + e.getMessage());
                    news.setPublishedAt(LocalDateTime.now());
                }
            } else {
                news.setPublishedAt(LocalDateTime.now());
            }

            // DB에 저장
            int result = newsMapper.insertRssNews(news);
            System.out.println("뉴스 저장 결과: " + result);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("newsId", newsId, "title", title));
            response.put("message", "뉴스가 저장되었습니다.");

            System.out.println("응답 데이터: " + response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("RSS 뉴스 저장 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "뉴스 저장 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 전체 뉴스 목록 조회 (카테고리 필터링 지원)
     * GET /api/news?page=1&size=20
     * GET /api/news?category=sports&page=1&size=20
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllNews(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        try {
            List<News> newsList;
            
            // 카테고리가 지정된 경우 해당 카테고리 뉴스만 조회
            if (category != null && !category.trim().isEmpty()) {
                newsList = rssService.getNewsByCategory(category, page, size);
            } else {
                // 카테고리가 없는 경우 전체 뉴스 조회
                newsList = rssService.getAllNews(page, size);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", newsList);
            response.put("totalCount", newsList.size());
            response.put("currentPage", page);
            response.put("totalPages", (int) Math.ceil(newsList.size() / (double) size));
            
            // 카테고리가 지정된 경우 카테고리 정보도 포함
            if (category != null) {
                response.put("category", category);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "뉴스 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 카테고리별 뉴스 조회
     * GET /api/news/category/it?page=1&size=10
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getNewsByCategory(
            @PathVariable String category,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        try {
            List<News> newsList = rssService.getNewsByCategory(category, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", newsList);
            response.put("category", category);
            response.put("page", page);
            response.put("size", size);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "카테고리별 뉴스 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 특정 뉴스 상세 조회
     * GET /api/news/123
     */
    @GetMapping("/{newsId}")
    public ResponseEntity<Map<String, Object>> getNewsById(
            @PathVariable("newsId") long newsId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            News news = rssService.getNewsById(newsId);
            
            Map<String, Object> response = new HashMap<>();
            if (news != null) {
                // 조회수 증가
                rssService.incrementViews((int) newsId);
                
                // 로그인한 사용자의 경우 열람기록 저장
                if (userPrincipal != null) {
                    try {
                        Integer userId = userPrincipal.getUserId();
                        viewHistoryService.addViewHistory(userId, (int) newsId);
                        System.out.println("열람기록 저장 성공: userId=" + userId + ", newsId=" + newsId);
                    } catch (Exception e) {
                        System.out.println("열람기록 저장 실패: " + e.getMessage());
                        // 열람기록 저장 실패해도 뉴스 조회는 계속 진행
                    }
                }
                
                response.put("success", true);
                response.put("data", news);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "뉴스를 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "뉴스 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * RSS 수집 수동 실행 (테스트용)
     * POST /api/news/collect
     */
    @PostMapping("/collect")
    public ResponseEntity<Map<String, Object>> collectRssNews() {
        try {
            rssService.collectRssNews();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "RSS 뉴스 수집이 시작되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "RSS 수집 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 뉴스 조회수 증가 (클릭 추적용)
     * POST /api/news/123/view
     */
    @PostMapping("/{newsId}/view")
    public ResponseEntity<Map<String, Object>> incrementViews(@PathVariable("newsId") long newsId) {
        try {
            rssService.incrementViews((int) newsId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "조회수가 증가되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "조회수 증가 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 동아일보 URL 형식 수정
     * POST /api/news/fix-donga-urls
     */
    @PostMapping("/fix-donga-urls")
    public ResponseEntity<Map<String, Object>> fixDongaUrls() {
        try {
            rssService.fixDongaUrlFormat();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "동아일보 URL 형식 수정이 완료되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "URL 형식 수정 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 뉴스 검색 (검색어 추적 포함)
     * GET /api/news/search?keyword=검색어&page=1&size=20
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchNews(
            @RequestParam String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        
        try {
            // 검색어 추적
            if (keyword != null && !keyword.trim().isEmpty()) {
                searchTrackingService.trackSearch(keyword.trim());
            }
            
            // 뉴스 검색
            List<News> newsList = rssService.searchNews(keyword, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", newsList);
            response.put("keyword", keyword);
            response.put("page", page);
            response.put("size", size);
            response.put("total", newsList.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "뉴스 검색 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 인기 뉴스 조회 (클릭 수 기준)
     * GET /api/news/popular?limit=10
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularNews(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        try {
            List<NewsClickTracking> popularNews = newsClickTrackingService.getTopClickedNews(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", popularNews);
            response.put("limit", limit);
            response.put("total", popularNews.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "인기 뉴스 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 기존 뉴스의 source 필드 업데이트 (null인 경우에만)
     * POST /api/news/update-source
     */
    @PostMapping("/update-source")
    public ResponseEntity<Map<String, Object>> updateNullSourceNews() {
        try {
            System.out.println("=== 기존 뉴스 source 필드 업데이트 API 호출 ===");
            
            rssService.updateNullSourceNews();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "기존 뉴스의 source 필드가 업데이트되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "source 필드 업데이트 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 기존 뉴스의 URL 필드 업데이트 (null인 경우에만)
     * POST /api/news/update-url
     */
    @PostMapping("/update-url")
    public ResponseEntity<Map<String, Object>> updateNullUrlNews() {
        try {
            System.out.println("=== 기존 뉴스 URL 필드 업데이트 API 호출 ===");
            
            rssService.updateNullUrlNews();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "기존 뉴스의 URL 필드가 업데이트되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "URL 필드 업데이트 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 기존 뉴스들의 카테고리를 올바르게 업데이트 (관리자용)
     * POST /api/news/update-categories
     */
    @PostMapping("/update-categories")
    public ResponseEntity<Map<String, Object>> updateNewsCategories() {
        try {
            System.out.println("=== 뉴스 카테고리 업데이트 API 호출 ===");
            
            // RssService의 updateNewsCategories 메서드 호출
            ((myapp.backend.domain.news.service.impl.RssServiceImpl) rssService).updateNewsCategories();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "뉴스 카테고리 업데이트가 완료되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("뉴스 카테고리 업데이트 실패: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "뉴스 카테고리 업데이트 실패: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 카테고리별 뉴스 개수 확인 (디버깅용)
     * GET /api/news/category-stats
     */
    @GetMapping("/category-stats")
    public ResponseEntity<Map<String, Object>> getCategoryStats() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            int generalCount = newsMapper.getCountByCategory("general");
            int economyCount = newsMapper.getCountByCategory("economy");
            int sportsCount = newsMapper.getCountByCategory("sports");
            int techCount = newsMapper.getCountByCategory("tech");
            
            response.put("success", true);
            response.put("general", generalCount);
            response.put("economy", economyCount);
            response.put("sports", sportsCount);
            response.put("tech", techCount);
            response.put("total", generalCount + economyCount + sportsCount + techCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "카테고리 통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

}