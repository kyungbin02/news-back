package myapp.backend.domain.search.service.impl;

import myapp.backend.domain.search.domain.RssNewsClickTracking;
import myapp.backend.domain.search.mapper.RssNewsClickTrackingMapper;
import myapp.backend.domain.search.service.RssNewsClickTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RssNewsClickTrackingServiceImpl implements RssNewsClickTrackingService {

    @Autowired
    private RssNewsClickTrackingMapper rssNewsClickTrackingMapper;

    @Override
    public void trackRssNewsClick(String rssNewsId, String newsTitle) {
        System.out.println(">>> RSS Service trackRssNewsClick 시작: rssNewsId=" + rssNewsId + ", newsTitle=" + newsTitle);
        
        if (rssNewsId == null || rssNewsId.trim().isEmpty()) {
            System.out.println(">>> 유효하지 않은 RSS 뉴스 ID로 종료");
            return; // 유효하지 않은 RSS 뉴스 ID는 무시
        }
        
        if (newsTitle == null) {
            newsTitle = ""; // null 방지
        }
        
        // 기존 클릭 추적 정보 확인
        System.out.println(">>> RSS DB 조회 시작: findByRssNewsId(" + rssNewsId + ")");
        RssNewsClickTracking existingTracking = rssNewsClickTrackingMapper.findByRssNewsId(rssNewsId);
        System.out.println(">>> RSS DB 조회 결과: " + (existingTracking != null ? "기존 데이터 존재" : "새로운 데이터"));
        
        if (existingTracking != null) {
            // 기존 뉴스의 클릭 횟수 증가
            System.out.println(">>> RSS 클릭 횟수 증가: " + rssNewsId);
            int updatedRows = rssNewsClickTrackingMapper.incrementClickCount(rssNewsId);
            System.out.println(">>> RSS 업데이트된 행 수: " + updatedRows);
        } else {
            // 새로운 뉴스 클릭 추적 생성
            System.out.println(">>> RSS 새로운 클릭 추적 생성: " + rssNewsId);
            RssNewsClickTracking newTracking = new RssNewsClickTracking(rssNewsId, newsTitle);
            int insertedRows = rssNewsClickTrackingMapper.insertRssNewsClickTracking(newTracking);
            System.out.println(">>> RSS 삽입된 행 수: " + insertedRows);
        }
        
        System.out.println(">>> RSS Service trackRssNewsClick 완료");
    }

    @Override
    public List<RssNewsClickTracking> getTopClickedRssNews(int limit) {
        System.out.println(">>> RSS Service getTopClickedRssNews 시작: limit=" + limit);
        List<RssNewsClickTracking> result = rssNewsClickTrackingMapper.findTopClickedRssNews(limit);
        System.out.println(">>> RSS Service getTopClickedRssNews 결과: " + result.size() + "개");
        return result;
    }

    @Override
    public List<RssNewsClickTracking> getRecentClickedRssNews(int limit) {
        System.out.println(">>> RSS Service getRecentClickedRssNews 시작: limit=" + limit);
        List<RssNewsClickTracking> result = rssNewsClickTrackingMapper.findRecentClickedRssNews(limit);
        System.out.println(">>> RSS Service getRecentClickedRssNews 결과: " + result.size() + "개");
        return result;
    }

    @Override
    public int getTotalCount() {
        return rssNewsClickTrackingMapper.getTotalCount();
    }
}







