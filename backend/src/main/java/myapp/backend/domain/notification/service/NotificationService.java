package myapp.backend.domain.notification.service;

import myapp.backend.domain.notification.vo.NotificationVO;
import java.util.List;

public interface NotificationService {
    
    // 사용자용 알림 (1단계)
    void notifyInquiryAnswer(int inquiryId, String inquiryTitle, int userId);
    void notifyBoardComment(int boardId, String boardTitle, String commenterUsername, int boardOwnerId);
    
    // 관리자용 알림 (2단계)
    void notifyNewUser(String username);
    void notifyReportBoard(int boardId, String boardTitle, String reporterUsername);
    void notifyReportComment(int commentId, String commentContent, String reporterUsername);
    void notifyInquiry(int inquiryId, String inquiryTitle, String inquirerUsername);
    
    // 알림 조회
    List<NotificationVO> getNotificationsByUserId(int userId);
    List<NotificationVO> getAdminNotifications();
    
    // 알림 읽음 처리
    void markAsRead(Long notificationId);
    
    // 디버깅용: 모든 알림 조회
    List<NotificationVO> getAllNotifications();
}
