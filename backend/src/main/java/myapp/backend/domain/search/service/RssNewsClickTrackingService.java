package myapp.backend.domain.search.service;

import myapp.backend.domain.search.domain.RssNewsClickTracking;
import java.util.List;

public interface RssNewsClickTrackingService {
    
    /**
     * RSS 뉴스 클릭 추적
     */
    void trackRssNewsClick(String rssNewsId, String newsTitle);
    
    /**
     * 인기 RSS 뉴스 조회 (클릭 횟수 기준 상위 N개)
     */
    List<RssNewsClickTracking> getTopClickedRssNews(int limit);
    
    /**
     * 최근 클릭된 RSS 뉴스 조회
     */
    List<RssNewsClickTracking> getRecentClickedRssNews(int limit);
    
    /**
     * 전체 RSS 뉴스 클릭 추적 개수 조회
     */
    int getTotalCount();
}







