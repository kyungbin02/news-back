package myapp.backend.domain.search.controller;

import myapp.backend.domain.search.domain.NewsClickTracking;
import myapp.backend.domain.search.domain.RssNewsClickTracking;
import myapp.backend.domain.search.service.NewsClickTrackingService;
import myapp.backend.domain.search.service.RssNewsClickTrackingService;
import myapp.backend.domain.news.service.RssService;
import myapp.backend.domain.news.domain.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/news-click")
// CORS는 SecurityConfig에서 전역으로 설정되어 있으므로 @CrossOrigin 제거
public class NewsClickTrackingController {

    @Autowired
    private NewsClickTrackingService newsClickTrackingService;

    @Autowired
    private RssNewsClickTrackingService rssNewsClickTrackingService;

    @Autowired
    private RssService rssService;

    // 중복 클릭 방지를 위한 임시 저장소 (뉴스ID -> 마지막 클릭 시간)
    private final Map<Integer, LocalDateTime> recentClicks = new ConcurrentHashMap<>();
    
    // IP 기반 중복 방지 (IP+뉴스ID -> 마지막 클릭 시간)
    private final Map<String, LocalDateTime> recentClicksByIp = new ConcurrentHashMap<>();
    
    // RSS 뉴스 중복 방지 (IP+RSS뉴스ID -> 마지막 클릭 시간)
    private final Map<String, LocalDateTime> recentRssClicksByIp = new ConcurrentHashMap<>();
    
    // 중복 방지 시간 간격 (초) - 페이지 이동 시 짧은 간격으로 중복 호출 차단
    private static final long DUPLICATE_PREVENTION_SECONDS = 5;

    /**
     * 뉴스 클릭 추적 (뉴스 클릭할 때마다 호출)
     * POST /api/news-click
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> trackNewsClick(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            System.out.println("=== 뉴스 클릭 추적 API 시작 ===");
            System.out.println("전체 요청 데이터: " + request);
            
            Object newsIdObj = request.get("newsId");
            System.out.println("newsIdObj: " + newsIdObj + " (타입: " + (newsIdObj != null ? newsIdObj.getClass().getSimpleName() : "null") + ")");
            
            // 여러 필드명 지원: newsTitle, title
            String newsTitle = (String) request.get("newsTitle");
            if (newsTitle == null || newsTitle.trim().isEmpty()) {
                newsTitle = (String) request.get("title");
            }
            System.out.println("newsTitle: " + newsTitle);
            
            if (newsIdObj == null) {
                System.out.println("ERROR: 뉴스 ID가 없음 - RSS 뉴스일 가능성");
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "RSS 뉴스 클릭 추적됨 (newsId 없음)");
                response.put("newsTitle", newsTitle);
                return ResponseEntity.ok(response);
            }
            
            Integer newsId;
            if (newsIdObj instanceof String) {
                String newsIdStr = (String) newsIdObj;
                try {
                    // 숫자로 변환 가능한지 확인
                    newsId = Integer.parseInt(newsIdStr);
                } catch (NumberFormatException e) {
                    // 문자열 ID인 경우 (예: "wui4ns") - RSS 뉴스로 처리
                    System.out.println("RSS 뉴스 ID 감지: " + newsIdStr + ", RSS 뉴스 클릭 추적 처리");
                    
                    // 클라이언트 IP 주소 추출
                    String clientIp = getClientIpAddress(httpRequest);
                    String ipRssNewsKey = clientIp + ":" + newsIdStr;
                    
                    // RSS 뉴스 중복 클릭 방지 체크
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime lastClickTime = recentRssClicksByIp.get(ipRssNewsKey);
                    
                    if (lastClickTime != null) {
                        long secondsElapsed = ChronoUnit.SECONDS.between(lastClickTime, now);
                        long millisElapsed = ChronoUnit.MILLIS.between(lastClickTime, now);
                        System.out.println("RSS 뉴스 동일 IP에서 이전 클릭으로부터 " + secondsElapsed + "초 (" + millisElapsed + "ms) 경과");
                        
                        // 5초 내 또는 동시 요청(100ms 내) 차단
                        if (secondsElapsed < DUPLICATE_PREVENTION_SECONDS || millisElapsed < 100) {
                            System.out.println("RSS 뉴스 중복 클릭 감지! " + DUPLICATE_PREVENTION_SECONDS + "초 내 또는 동시 요청으로 인한 동일 IP 재클릭 - 무시됨");
                            
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("message", "RSS 뉴스 중복 클릭 방지됨 (조회수 증가 안됨)");
                            response.put("rssNewsId", newsIdStr);
                            response.put("clientIp", clientIp);
                            response.put("duplicatePrevented", true);
                            response.put("secondsElapsed", secondsElapsed);
                            response.put("millisElapsed", millisElapsed);
                            return ResponseEntity.ok(response);
                        }
                    }
                    
                    // 중복이 아니므로 클릭 시간 기록
                    recentRssClicksByIp.put(ipRssNewsKey, now);
                    System.out.println("RSS 뉴스 정상 클릭으로 처리 - 조회수 증가 진행");
                    
                    // RSS 뉴스 클릭 추적
                    System.out.println("RSS Service 호출 시작: trackRssNewsClick(" + newsIdStr + ", " + newsTitle + ")");
                    rssNewsClickTrackingService.trackRssNewsClick(newsIdStr, newsTitle);
                    System.out.println("RSS Service 호출 완료");
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "RSS 뉴스 클릭이 추적되었습니다.");
                    response.put("rssNewsId", newsIdStr);
                    response.put("newsTitle", newsTitle);
                    response.put("duplicatePrevented", false);
                    
                    System.out.println("=== RSS 뉴스 클릭 추적 API 완료 ===");
                    return ResponseEntity.ok(response);
                }
            } else if (newsIdObj instanceof Integer) {
                newsId = (Integer) newsIdObj;
            } else {
                System.out.println("ERROR: 유효하지 않은 뉴스 ID 형식: " + newsIdObj.getClass().getSimpleName());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "유효하지 않은 뉴스 ID 형식입니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 클라이언트 IP 주소 추출
            String clientIp = getClientIpAddress(httpRequest);
            String ipNewsKey = clientIp + ":" + newsId;
            
            System.out.println("뉴스 ID: " + newsId + ", 제목: " + newsTitle + ", 클라이언트 IP: " + clientIp);
            System.out.println("전달받은 요청 데이터: " + request.toString());
            
            // 중복 클릭 방지 체크 (IP + 뉴스ID 기준)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastClickTime = recentClicksByIp.get(ipNewsKey);
            
            if (lastClickTime != null) {
                long secondsElapsed = ChronoUnit.SECONDS.between(lastClickTime, now);
                long millisElapsed = ChronoUnit.MILLIS.between(lastClickTime, now);
                System.out.println("동일 IP에서 이전 클릭으로부터 " + secondsElapsed + "초 (" + millisElapsed + "ms) 경과");
                
                // 5초 내 또는 동시 요청(100ms 내) 차단
                if (secondsElapsed < DUPLICATE_PREVENTION_SECONDS || millisElapsed < 100) {
                    System.out.println("중복 클릭 감지! " + DUPLICATE_PREVENTION_SECONDS + "초 내 또는 동시 요청으로 인한 동일 IP 재클릭 - 무시됨");
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "중복 클릭 방지됨 (조회수 증가 안됨)");
                    response.put("newsId", newsId);
                    response.put("clientIp", clientIp);
                    response.put("duplicatePrevented", true);
                    response.put("secondsElapsed", secondsElapsed);
                    response.put("millisElapsed", millisElapsed);
                    return ResponseEntity.ok(response);
                }
            }
            
            // 중복이 아니므로 클릭 시간 기록 (IP 기준)
            recentClicksByIp.put(ipNewsKey, now);
            // 기존 방식도 유지 (뉴스ID 기준)
            recentClicks.put(newsId, now);
            System.out.println("정상 클릭으로 처리 - 조회수 증가 진행");
            
            // 제목이 여전히 비어있으면 실제 뉴스에서 가져오기
            if (newsTitle == null || newsTitle.trim().isEmpty()) {
                News actualNews = rssService.getNewsById(newsId);
                if (actualNews != null) {
                    newsTitle = actualNews.getTitle();
                    System.out.println("빈 제목을 실제 뉴스에서 가져옴: " + newsTitle);
                } else {
                    newsTitle = "";
                }
            }
            
            System.out.println("Service 호출 시작: trackNewsClick(" + newsId + ", " + newsTitle + ")");
            newsClickTrackingService.trackNewsClick(newsId, newsTitle);
            System.out.println("Service 호출 완료");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "뉴스 클릭이 추적되었습니다.");
            response.put("newsId", newsId);
            response.put("newsTitle", newsTitle);
            response.put("duplicatePrevented", false);
            
            System.out.println("=== 뉴스 클릭 추적 API 완료 ===");
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "유효하지 않은 뉴스 ID입니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "뉴스 클릭 추적 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 인기 뉴스 조회 (클릭수 기준) - 최신뉴스 + RSS 뉴스 통합
     * GET /api/news-click/top?limit=10
     */
    @GetMapping("/top")
    public ResponseEntity<Map<String, Object>> getTopClickedNews(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        try {
            // 최신뉴스와 RSS 뉴스 각각 조회
            List<NewsClickTracking> topNews = newsClickTrackingService.getTopClickedNews(limit);
            List<RssNewsClickTracking> topRssNews = rssNewsClickTrackingService.getTopClickedRssNews(limit);
            
            // 통합된 인기뉴스 리스트 생성
            List<Map<String, Object>> combinedNews = new ArrayList<>();
            
            // 최신뉴스 추가
            for (NewsClickTracking news : topNews) {
                Map<String, Object> newsItem = new HashMap<>();
                newsItem.put("newsId", news.getNewsId());
                newsItem.put("newsTitle", news.getNewsTitle());
                newsItem.put("source", news.getSource());  // source 필드 추가
                newsItem.put("clickCount", news.getClickCount());
                newsItem.put("lastClickedAt", news.getLastClickedAt());
                newsItem.put("newsType", "db"); // 최신뉴스 구분
                combinedNews.add(newsItem);
            }
            
            // RSS 뉴스 추가
            for (RssNewsClickTracking rssNews : topRssNews) {
                Map<String, Object> newsItem = new HashMap<>();
                newsItem.put("newsId", rssNews.getRssNewsId());
                newsItem.put("newsTitle", rssNews.getNewsTitle());
                newsItem.put("clickCount", rssNews.getClickCount());
                newsItem.put("lastClickedAt", rssNews.getLastClickedAt());
                newsItem.put("newsType", "rss"); // RSS 뉴스 구분
                combinedNews.add(newsItem);
            }
            
            // 클릭수 기준으로 정렬 (내림차순)
            combinedNews.sort((a, b) -> {
                Integer clickCountA = (Integer) a.get("clickCount");
                Integer clickCountB = (Integer) b.get("clickCount");
                return clickCountB.compareTo(clickCountA);
            });
            
            // limit만큼만 반환
            if (combinedNews.size() > limit) {
                combinedNews = combinedNews.subList(0, limit);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", combinedNews);
            response.put("limit", limit);
            response.put("total", combinedNews.size());
            response.put("dbNewsCount", topNews.size());
            response.put("rssNewsCount", topRssNews.size());
            
            System.out.println("통합 인기뉴스 조회 완료: 총 " + combinedNews.size() + "개 (DB:" + topNews.size() + ", RSS:" + topRssNews.size() + ")");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "인기 뉴스 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 최근 클릭된 뉴스 조회
     * GET /api/news-click/recent?limit=10
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentClickedNews(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        try {
            List<NewsClickTracking> recentNews = newsClickTrackingService.getRecentClickedNews(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", recentNews);
            response.put("limit", limit);
            response.put("total", recentNews.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "최근 클릭 뉴스 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 전체 클릭 추적 목록 조회 (페이징)
     * GET /api/news-click?page=1&size=20
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllNewsClickTracking(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            List<NewsClickTracking> clickTrackings = newsClickTrackingService.getAllNewsClickTracking(page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", clickTrackings);
            response.put("page", page);
            response.put("size", size);
            response.put("total", clickTrackings.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "클릭 추적 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 특정 뉴스의 클릭 정보 조회
     * GET /api/news-click/news/{newsId}
     */
    @GetMapping("/news/{newsId}")
    public ResponseEntity<Map<String, Object>> getNewsClickInfo(@PathVariable("newsId") Integer newsId) {
        try {
            NewsClickTracking clickInfo = newsClickTrackingService.getNewsClickInfo(newsId);
            
            Map<String, Object> response = new HashMap<>();
            if (clickInfo != null) {
                response.put("success", true);
                response.put("data", clickInfo);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "뉴스 클릭 정보를 찾을 수 없습니다.");
                response.put("clickCount", 0);
                return ResponseEntity.ok(response); // 404 대신 0으로 반환
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "뉴스 클릭 정보 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 특정 뉴스의 클릭 수만 조회
     * GET /api/news-click/count/{newsId}
     */
    @GetMapping("/count/{newsId}")
    public ResponseEntity<Map<String, Object>> getClickCount(@PathVariable("newsId") Integer newsId) {
        try {
            int clickCount = newsClickTrackingService.getClickCount(newsId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newsId", newsId);
            response.put("clickCount", clickCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "클릭 수 조회 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 클릭 기록 정리 (관리자용)
     * DELETE /api/news-click/cleanup
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupClickTracking() {
        try {
            newsClickTrackingService.cleanupOldClickTracking();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "클릭 기록 정리가 완료되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "클릭 기록 정리 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_CLIENT_IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        
        // 여러 IP가 있는 경우 첫 번째 IP 사용
        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }
        
        return clientIp != null ? clientIp : "unknown";
    }

    /**
     * 중복 방지 캐시 정리 (관리자용)
     * POST /api/news-click/clear-cache
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<Map<String, Object>> clearDuplicatePreventionCache() {
        try {
            int beforeSizeBasic = recentClicks.size();
            int beforeSizeIp = recentClicksByIp.size();
            
            // 5분 이상 된 데이터 정리
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            recentClicks.entrySet().removeIf(entry -> entry.getValue().isBefore(fiveMinutesAgo));
            recentClicksByIp.entrySet().removeIf(entry -> entry.getValue().isBefore(fiveMinutesAgo));
            
            int afterSizeBasic = recentClicks.size();
            int afterSizeIp = recentClicksByIp.size();
            int clearedCount = (beforeSizeBasic - afterSizeBasic) + (beforeSizeIp - afterSizeIp);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "중복 방지 캐시 정리 완료");
            response.put("beforeSizeBasic", beforeSizeBasic);
            response.put("beforeSizeIp", beforeSizeIp);
            response.put("afterSizeBasic", afterSizeBasic);
            response.put("afterSizeIp", afterSizeIp);
            response.put("clearedCount", clearedCount);
            
            System.out.println("중복 방지 캐시 정리: " + clearedCount + "개 항목 삭제됨 (기본:" + (beforeSizeBasic - afterSizeBasic) + ", IP:" + (beforeSizeIp - afterSizeIp) + ")");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "캐시 정리 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 빈 제목 수정 (관리자용)
     * POST /api/news-click/fix-empty-titles
     */
    @PostMapping("/fix-empty-titles")
    public ResponseEntity<Map<String, Object>> fixEmptyTitles() {
        try {
            List<NewsClickTracking> emptyTitleRecords = newsClickTrackingService.getAllNewsClickTracking(1, 1000);
            int fixedCount = 0;
            
            for (NewsClickTracking record : emptyTitleRecords) {
                if (record.getNewsTitle() == null || record.getNewsTitle().trim().isEmpty()) {
                    News actualNews = rssService.getNewsById(record.getNewsId());
                    if (actualNews != null && actualNews.getTitle() != null && !actualNews.getTitle().trim().isEmpty()) {
                        newsClickTrackingService.updateNewsTitle(record.getNewsId(), actualNews.getTitle());
                        fixedCount++;
                        System.out.println("수정됨: newsId=" + record.getNewsId() + ", title=" + actualNews.getTitle());
                    }
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "빈 제목 수정이 완료되었습니다.");
            response.put("fixedCount", fixedCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "빈 제목 수정 실패: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

