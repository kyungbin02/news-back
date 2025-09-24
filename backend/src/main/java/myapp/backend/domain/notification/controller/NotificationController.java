package myapp.backend.domain.notification.controller;

import myapp.backend.domain.notification.service.NotificationService;
import myapp.backend.domain.notification.vo.NotificationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // 사용자 알림 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationVO>> getUserNotifications(@PathVariable("userId") int userId) {
        try {
            System.out.println("[NotificationController] 사용자 알림 조회 요청 - userId: " + userId);
            List<NotificationVO> notifications = notificationService.getNotificationsByUserId(userId);
            System.out.println("[NotificationController] 조회된 알림 개수: " + (notifications != null ? notifications.size() : "null"));
            if (notifications != null && !notifications.isEmpty()) {
                System.out.println("[NotificationController] 첫 번째 알림: " + notifications.get(0));
            } else {
                System.out.println("[NotificationController] 알림 데이터가 없습니다.");
            }
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("사용자 알림 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 관리자 알림 조회
    @GetMapping("/admin")
    public ResponseEntity<List<NotificationVO>> getAdminNotifications() {
        try {
            List<NotificationVO> notifications = notificationService.getAdminNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("관리자 알림 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 알림 읽음 처리
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable("notificationId") Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok("알림을 읽음 처리했습니다.");
        } catch (Exception e) {
            System.err.println("알림 읽음 처리 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 디버깅용: 데이터베이스 직접 확인
    @GetMapping("/debug/user/{userId}")
    public ResponseEntity<String> debugUserNotifications(@PathVariable("userId") int userId) {
        try {
            System.out.println("[NotificationController] 디버깅 - 사용자 ID: " + userId);
            
            // 1. JWT 토큰에서 추출한 사용자 ID 확인
            System.out.println("[NotificationController] JWT에서 추출한 사용자 ID: " + userId);
            
            // 2. 데이터베이스에서 직접 조회
            List<NotificationVO> notifications = notificationService.getNotificationsByUserId(userId);
            System.out.println("[NotificationController] 조회된 알림 개수: " + (notifications != null ? notifications.size() : "null"));
            
            // 3. 모든 알림 출력
            if (notifications != null && !notifications.isEmpty()) {
                for (int i = 0; i < notifications.size(); i++) {
                    System.out.println("[NotificationController] 알림 " + (i + 1) + ": " + notifications.get(i));
                }
            }
            
            return ResponseEntity.ok("디버깅 완료 - 콘솔 로그 확인");
        } catch (Exception e) {
            System.err.println("디버깅 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 디버깅용: 모든 알림 조회
    @GetMapping("/debug/all")
    public ResponseEntity<String> debugAllNotifications() {
        try {
            System.out.println("[NotificationController] 모든 알림 조회 시작");
            List<NotificationVO> allNotifications = notificationService.getAllNotifications();
            System.out.println("[NotificationController] 전체 알림 개수: " + (allNotifications != null ? allNotifications.size() : "null"));
            
            if (allNotifications != null && !allNotifications.isEmpty()) {
                for (int i = 0; i < allNotifications.size(); i++) {
                    System.out.println("[NotificationController] 전체 알림 " + (i + 1) + ": " + allNotifications.get(i));
                }
            } else {
                System.out.println("[NotificationController] 데이터베이스에 알림이 없습니다.");
            }
            
            return ResponseEntity.ok("전체 알림 조회 완료 - 콘솔 로그 확인");
        } catch (Exception e) {
            System.err.println("전체 알림 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}