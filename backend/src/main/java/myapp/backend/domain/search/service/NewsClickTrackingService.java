package myapp.backend.domain.search.service;

import myapp.backend.domain.search.domain.NewsClickTracking;
import java.util.List;

public interface NewsClickTrackingService {
    
    /**
     * 뉴스 클릭 추적 (뉴스 클릭할 때마다 호출)
     * @param newsId 뉴스 ID
     * @param newsTitle 뉴스 제목
     */
    void trackNewsClick(Integer newsId, String newsTitle);
    
    /**
     * 인기 뉴스 조회 (클릭수 기준)
     * @param limit 조회할 개수
     * @return 인기 뉴스 목록
     */
    List<NewsClickTracking> getTopClickedNews(int limit);
    
    /**
     * 최근 클릭된 뉴스 조회
     * @param limit 조회할 개수
     * @return 최근 클릭 뉴스 목록
     */
    List<NewsClickTracking> getRecentClickedNews(int limit);
    
    /**
     * 전체 클릭 추적 목록 조회 (페이징)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 클릭 추적 목록
     */
    List<NewsClickTracking> getAllNewsClickTracking(int page, int size);
    
    /**
     * 특정 뉴스의 클릭 정보 조회
     * @param newsId 뉴스 ID
     * @return 클릭 추적 정보
     */
    NewsClickTracking getNewsClickInfo(Integer newsId);
    
    /**
     * 특정 뉴스의 총 클릭 수 조회
     * @param newsId 뉴스 ID
     * @return 클릭 수
     */
    int getClickCount(Integer newsId);
    
    /**
     * 오래된 클릭 기록 정리 (배치 작업용)
     */
    void cleanupOldClickTracking();
    
    /**
     * 뉴스 제목 업데이트
     * @param newsId 뉴스 ID
     * @param newsTitle 새로운 제목
     */
    void updateNewsTitle(Integer newsId, String newsTitle);
}

