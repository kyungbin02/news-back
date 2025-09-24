package myapp.backend.domain.search.service.impl;

import myapp.backend.domain.search.domain.SearchKeyword;
import myapp.backend.domain.search.mapper.SearchKeywordMapper;
import myapp.backend.domain.search.service.SearchTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class SearchTrackingServiceImpl implements SearchTrackingService {

    @Autowired
    private SearchKeywordMapper searchKeywordMapper;
    
    // 중복 검색 방지를 위한 캐시 (검색어: 마지막 검색 시간)
    private final ConcurrentHashMap<String, LocalDateTime> recentSearches = new ConcurrentHashMap<>();
    
    // 중복 방지 시간 (초)
    private static final int DUPLICATE_PREVENTION_SECONDS = 3;

    @Override
    public void trackSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return; // 빈 검색어는 무시
        }
        
        keyword = keyword.trim();
        
        // 중복 검색 방지 체크
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastSearchTime = recentSearches.get(keyword);
        
        if (lastSearchTime != null) {
            long secondsElapsed = ChronoUnit.SECONDS.between(lastSearchTime, now);
            long millisElapsed = ChronoUnit.MILLIS.between(lastSearchTime, now);
            
            if (secondsElapsed < DUPLICATE_PREVENTION_SECONDS || millisElapsed < 100) {
                System.out.println("중복 검색 감지! " + DUPLICATE_PREVENTION_SECONDS + "초 내 또는 동시 요청으로 인한 동일 검색어 재검색 - 무시됨: " + keyword);
                return;
            }
        }
        
        // 검색 시간 기록
        recentSearches.put(keyword, now);
        
        // 기존 검색어 확인
        SearchKeyword existingKeyword = searchKeywordMapper.findByKeyword(keyword);
        
        if (existingKeyword != null) {
            // 기존 검색어인 경우 카운트 증가
            searchKeywordMapper.incrementSearchCount(keyword);
            System.out.println("검색어 카운트 증가: " + keyword);
        } else {
            // 새로운 검색어인 경우 삽입
            SearchKeyword newKeyword = new SearchKeyword(keyword);
            searchKeywordMapper.insertSearchKeyword(newKeyword);
            System.out.println("새로운 검색어 저장: " + keyword);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchKeyword> getTopSearchKeywords(int limit) {
        if (limit <= 0) limit = 10; // 기본값
        return searchKeywordMapper.findTopSearchKeywords(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchKeyword> getRecentSearchKeywords(int limit) {
        if (limit <= 0) limit = 10; // 기본값
        return searchKeywordMapper.findRecentSearchKeywords(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchKeyword> getAllSearchKeywords(int page, int size) {
        if (page <= 0) page = 1;
        if (size <= 0) size = 20;
        
        int offset = (page - 1) * size;
        return searchKeywordMapper.findAllSearchKeywords(offset, size);
    }

    @Override
    @Transactional(readOnly = true)
    public SearchKeyword getSearchKeywordInfo(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return searchKeywordMapper.findByKeyword(keyword.trim());
    }

    @Override
    public void cleanupOldSearchKeywords() {
        try {
            // 30일 이상 된 검색어 삭제
            int deletedOld = searchKeywordMapper.deleteOldSearchKeywords();
            System.out.println("오래된 검색어 삭제: " + deletedOld + "개");
            
            // 1회만 검색된 7일 이상 된 검색어 삭제
            int deletedLowCount = searchKeywordMapper.deleteLowCountSearchKeywords();
            System.out.println("낮은 빈도 검색어 삭제: " + deletedLowCount + "개");
            
        } catch (Exception e) {
            System.err.println("검색어 정리 실패: " + e.getMessage());
        }
    }

    /**
     * 검색어 통계 정보 조회
     */
    @Transactional(readOnly = true)
    public int getTotalSearchKeywordCount() {
        return searchKeywordMapper.getTotalCount();
    }
}


