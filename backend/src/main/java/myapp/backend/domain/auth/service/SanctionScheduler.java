package myapp.backend.domain.auth.service;

import myapp.backend.domain.auth.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
public class SanctionScheduler {

    @Autowired
    private UserMapper userMapper;

    // 매일 자정에 만료된 제재를 자동 해제
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkExpiredSanctions() {
        try {
            // 만료된 정지 계정들을 active로 변경
            int updatedCount = userMapper.updateExpiredSanctions();
            if (updatedCount > 0) {
                System.out.println("만료된 제재 " + updatedCount + "건을 자동 해제했습니다.");
            }
        } catch (Exception e) {
            System.err.println("제재 만료 체크 중 오류 발생: " + e.getMessage());
        }
    }
}
