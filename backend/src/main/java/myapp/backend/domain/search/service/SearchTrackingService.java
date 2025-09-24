package myapp.backend.domain.search.service;

import myapp.backend.domain.search.domain.SearchKeyword;
import java.util.List;

public interface SearchTrackingService {
    
    /**
     * 검색어 추적 (검색할 때마다 호출)
     * @param keyword 검색어
     */
    void trackSearch(String keyword);
    
    /**
     * 인기 검색어 조회 (실시간 검색어)
     * @param limit 조회할 개수
     * @return 인기 검색어 목록
     */
    List<SearchKeyword> getTopSearchKeywords(int limit);
    
    /**
     * 최근 검색어 조회
     * @param limit 조회할 개수
     * @return 최근 검색어 목록
     */
    List<SearchKeyword> getRecentSearchKeywords(int limit);
    
    /**
     * 전체 검색어 목록 조회 (페이징)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 검색어 목록
     */
    List<SearchKeyword> getAllSearchKeywords(int page, int size);
    
    /**
     * 특정 검색어 정보 조회
     * @param keyword 검색어
     * @return 검색어 정보
     */
    SearchKeyword getSearchKeywordInfo(String keyword);
    
    /**
     * 오래된 검색어 정리 (배치 작업용)
     */
    void cleanupOldSearchKeywords();
}


