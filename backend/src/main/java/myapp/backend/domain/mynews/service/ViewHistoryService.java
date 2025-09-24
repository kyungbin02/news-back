package myapp.backend.domain.mynews.service;

import myapp.backend.domain.mynews.domain.ViewHistory;
import java.util.List;

public interface ViewHistoryService {
    
    /**
     * 조회 기록 추가
     */
    ViewHistory addViewHistory(Integer userId, Integer newsId);
    
    /**
     * 조회 기록 추가 (읽은 시간 포함)
     */
    ViewHistory addViewHistory(Integer userId, Integer newsId, Integer readTime);
    
    /**
     * 읽은 시간 업데이트
     */
    boolean updateReadTime(Integer viewId, Integer userId, Integer readTime);
    
    /**
     * 사용자의 조회 기록 조회 (제한된 개수)
     */
    List<ViewHistory> getViewHistoryByUserId(Integer userId, int limit);
    
    /**
     * 사용자의 조회 기록 조회 (전체)
     */
    List<ViewHistory> getViewHistoryByUserId(Integer userId);
    
    /**
     * 특정 뉴스의 최신 조회 기록 조회
     */
    ViewHistory getLatestViewHistoryByUserIdAndNewsId(Integer userId, Integer newsId);
    
    /**
     * 사용자의 조회 기록 개수 조회
     */
    int getViewHistoryCountByUserId(Integer userId);
    
    /**
     * 사용자의 총 읽은 시간 조회
     */
    Integer getTotalReadTimeByUserId(Integer userId);
}






