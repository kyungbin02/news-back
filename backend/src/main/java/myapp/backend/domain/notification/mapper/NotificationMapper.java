package myapp.backend.domain.notification.mapper;

import myapp.backend.domain.notification.vo.NotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NotificationMapper {
    
    // 알림 저장
    void insertNotification(NotificationVO notification);
    
    // 사용자 알림 조회
    List<NotificationVO> getNotificationsByUserId(@Param("user_id") int userId);
    
    // 관리자 알림 조회
    List<NotificationVO> getAdminNotifications();
    
    // 알림 읽음 처리
    void markAsRead(@Param("id") Long notificationId);
    
    // 디버깅용: 모든 알림 조회
    List<NotificationVO> getAllNotifications();
}
