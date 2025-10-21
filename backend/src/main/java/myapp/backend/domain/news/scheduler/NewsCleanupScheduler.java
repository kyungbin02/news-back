package myapp.backend.domain.news.scheduler;

import myapp.backend.domain.news.mapper.NewsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NewsCleanupScheduler {

    @Autowired
    private NewsMapper newsMapper;

    /**
     * 매주 월요일 오전 9시에 뉴스 통계 리포트
     * RSS 수집 시마다 자동 정리가 실행되므로 별도 정리 스케줄러는 불필요
     */
    @Scheduled(cron = "0 0 9 * * MON")
    public void weeklyNewsReport() {
        try {
            System.out.println("=== 주간 뉴스 통계 리포트: " + java.time.LocalDateTime.now() + " ===");
            
            int totalNews = newsMapper.getTotalCount();
            int economyNews = newsMapper.getCountByCategory("economy");
            int sportsNews = newsMapper.getCountByCategory("sports");
            int techNews = newsMapper.getCountByCategory("tech");
            int generalNews = newsMapper.getCountByCategory("general");
            
            System.out.println("총 뉴스 개수: " + totalNews);
            System.out.println("경제 뉴스: " + economyNews);
            System.out.println("스포츠 뉴스: " + sportsNews);
            System.out.println("IT 뉴스: " + techNews);
            System.out.println("일반 뉴스: " + generalNews);
            System.out.println("=== 주간 뉴스 통계 리포트 완료 ===");
            
        } catch (Exception e) {
            System.err.println("뉴스 통계 리포트 생성 중 오류 발생: " + e.getMessage());
        }
    }
}
