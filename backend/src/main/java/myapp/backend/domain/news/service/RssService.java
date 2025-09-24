package myapp.backend.domain.news.service;

import myapp.backend.domain.news.domain.News;
import java.util.List;

public interface RssService {
    
    /**
     * RSS 피드에서 뉴스를 수집하여 DB에 저장
     * 스케줄러에 의해 5분마다 자동 실행
     */
    void collectRssNews();
    
    /**
     * 전체 뉴스 목록 조회 (페이징)
     */
    List<News> getAllNews(int page, int size);
    
    /**
     * 카테고리별 뉴스 목록 조회 (페이징)
     */
    List<News> getNewsByCategory(String category, int page, int size);
    
    /**
     * 뉴스 ID로 특정 뉴스 조회
     */
    News getNewsById(int newsId);
    
    /**
     * 뉴스 조회수 증가
     */
    void incrementViews(int newsId);
    
    /**
     * 뉴스 검색 (제목, 내용 기준)
     */
    List<News> searchNews(String keyword, int page, int size);
    
    /**
     * 인기 뉴스 조회 (조회수 기준)
     */
    List<News> getPopularNews(int limit);
}