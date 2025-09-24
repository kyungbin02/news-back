package myapp.backend.domain.search.service.impl;

import myapp.backend.domain.search.domain.NewsClickTracking;
import myapp.backend.domain.search.mapper.NewsClickTrackingMapper;
import myapp.backend.domain.search.service.NewsClickTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NewsClickTrackingServiceImpl implements NewsClickTrackingService {

    @Autowired
    private NewsClickTrackingMapper newsClickTrackingMapper;

    @Override
    public void trackNewsClick(Integer newsId, String newsTitle) {
        System.out.println(">>> Service trackNewsClick 시작: newsId=" + newsId + ", newsTitle=" + newsTitle);
        System.out.println(">>> 뉴스 ID 타입: " + newsId.getClass().getSimpleName());
        
        if (newsId == null || newsId <= 0) {
            System.out.println(">>> 유효하지 않은 뉴스 ID로 종료");
            return; // 유효하지 않은 뉴스 ID는 무시
        }
        
        if (newsTitle == null) {
            newsTitle = ""; // null 방지
        }
        
        // 기존 클릭 추적 정보 확인
        System.out.println(">>> DB 조회 시작: findByNewsId(" + newsId + ")");
        NewsClickTracking existingTracking = newsClickTrackingMapper.findByNewsId(newsId);
        System.out.println(">>> DB 조회 결과: " + (existingTracking != null ? "기존 데이터 존재" : "새로운 데이터"));
        
        if (existingTracking != null) {
            // 기존 추적 정보가 있는 경우 클릭 수 증가
            System.out.println(">>> DB 업데이트 시작: incrementClickCount(" + newsId + ")");
            int updateResult = newsClickTrackingMapper.incrementClickCount(newsId);
            System.out.println(">>> DB 업데이트 결과: " + updateResult + "행 영향받음");
            
            // 제목이 다르면 업데이트 (뉴스 제목이 변경된 경우)
            if (!newsTitle.equals(existingTracking.getNewsTitle())) {
                System.out.println(">>> 제목 업데이트: " + existingTracking.getNewsTitle() + " -> " + newsTitle);
                newsClickTrackingMapper.updateNewsTitle(newsId, newsTitle);
            }
            
            System.out.println(">>> 뉴스 클릭 수 증가 완료: " + newsId + " - " + newsTitle);
        } else {
            // 새로운 뉴스 클릭인 경우 삽입
            System.out.println(">>> DB 삽입 시작: insertNewsClickTracking");
            NewsClickTracking newTracking = new NewsClickTracking(newsId, newsTitle);
            int insertResult = newsClickTrackingMapper.insertNewsClickTracking(newTracking);
            System.out.println(">>> DB 삽입 결과: " + insertResult + "행 삽입됨");
            System.out.println(">>> 새로운 뉴스 클릭 추적 시작 완료: " + newsId + " - " + newsTitle);
        }
        
        System.out.println(">>> Service trackNewsClick 완료");
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsClickTracking> getTopClickedNews(int limit) {
        if (limit <= 0) limit = 10; // 기본값
        return newsClickTrackingMapper.findTopClickedNews(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsClickTracking> getRecentClickedNews(int limit) {
        if (limit <= 0) limit = 10; // 기본값
        return newsClickTrackingMapper.findRecentClickedNews(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsClickTracking> getAllNewsClickTracking(int page, int size) {
        if (page <= 0) page = 1;
        if (size <= 0) size = 20;
        
        int offset = (page - 1) * size;
        return newsClickTrackingMapper.findAllNewsClickTracking(offset, size);
    }

    @Override
    @Transactional(readOnly = true)
    public NewsClickTracking getNewsClickInfo(Integer newsId) {
        if (newsId == null || newsId <= 0) {
            return null;
        }
        return newsClickTrackingMapper.findByNewsId(newsId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getClickCount(Integer newsId) {
        if (newsId == null || newsId <= 0) {
            return 0;
        }
        return newsClickTrackingMapper.getClickCountByNewsId(newsId);
    }

    @Override
    public void cleanupOldClickTracking() {
        try {
            // 90일 이상 된 클릭 기록 삭제
            int deletedOld = newsClickTrackingMapper.deleteOldClickTracking();
            System.out.println("오래된 클릭 기록 삭제: " + deletedOld + "개");
            
            // 1회만 클릭된 30일 이상 된 기록 삭제
            int deletedLowClick = newsClickTrackingMapper.deleteLowClickTracking();
            System.out.println("낮은 클릭 기록 삭제: " + deletedLowClick + "개");
            
        } catch (Exception e) {
            System.err.println("클릭 기록 정리 실패: " + e.getMessage());
        }
    }

    /**
     * 클릭 추적 통계 정보 조회
     */
    @Transactional(readOnly = true)
    public int getTotalClickTrackingCount() {
        return newsClickTrackingMapper.getTotalCount();
    }

    @Override
    public void updateNewsTitle(Integer newsId, String newsTitle) {
        if (newsId != null && newsTitle != null) {
            newsClickTrackingMapper.updateNewsTitle(newsId, newsTitle);
            System.out.println("뉴스 제목 업데이트 완료: " + newsId + " -> " + newsTitle);
        }
    }
}

