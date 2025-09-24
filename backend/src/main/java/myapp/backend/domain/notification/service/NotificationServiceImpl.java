package myapp.backend.domain.notification.service;

import myapp.backend.domain.notification.mapper.NotificationMapper;
import myapp.backend.domain.notification.vo.NotificationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationMapper notificationMapper;

    // 사용자용 알림 (1단계)
    @Override
    public void notifyInquiryAnswer(int inquiryId, String inquiryTitle, int userId) {
        try {
            // 알림 데이터 생성
            NotificationVO notification = new NotificationVO();
            notification.setUser_id(userId);
            notification.setAdmin_id(null);
            notification.setNotification_type("INQUIRY_ANSWER");
            notification.setNotification_title("문의사항 답변");
            notification.setNotification_message("'" + inquiryTitle + "' 문의사항에 답변이 등록되었습니다.");
            notification.setIs_read(false);

            // DB에 저장
            notificationMapper.insertNotification(notification);

            // WebSocket으로 실시간 전송
            messagingTemplate.convertAndSend("/queue/user/" + userId + "/notifications", notification);

            System.out.println("[NotificationService] 문의사항 답변 알림 전송 완료 - userId: " + userId);
        } catch (Exception e) {
            System.err.println("[NotificationService] 문의사항 답변 알림 전송 실패: " + e.getMessage());
        }
    }

    @Override
    public void notifyBoardComment(int boardId, String boardTitle, String commenterUsername, int boardOwnerId) {
        try {
            // 알림 데이터 생성
            NotificationVO notification = new NotificationVO();
            notification.setUser_id(boardOwnerId);
            notification.setAdmin_id(null);
            notification.setNotification_type("BOARD_COMMENT");
            notification.setNotification_title("게시글 댓글");
            notification.setNotification_message("'" + boardTitle + "' 게시글에 " + commenterUsername + "님이 댓글을 남겼습니다.");
            notification.setIs_read(false);

            // DB에 저장
            notificationMapper.insertNotification(notification);

            // WebSocket으로 실시간 전송
            messagingTemplate.convertAndSend("/queue/user/" + boardOwnerId + "/notifications", notification);

            System.out.println("[NotificationService] 게시글 댓글 알림 전송 완료 - boardOwnerId: " + boardOwnerId);
        } catch (Exception e) {
            System.err.println("[NotificationService] 게시글 댓글 알림 전송 실패: " + e.getMessage());
        }
    }

    // 관리자용 알림 (2단계)
    @Override
    public void notifyNewUser(String username) {
        try {
            // 알림 데이터 생성
            NotificationVO notification = new NotificationVO();
            notification.setUser_id(null);
            notification.setAdmin_id(1); // 관리자 ID (임시로 1 설정)
            notification.setNotification_type("NEW_USER");
            notification.setNotification_title("신규가입");
            notification.setNotification_message(username + "님이 새로 가입했습니다.");
            notification.setIs_read(false);

            // DB에 저장
            notificationMapper.insertNotification(notification);

            // WebSocket으로 관리자에게 브로드캐스트 전송
            messagingTemplate.convertAndSend("/topic/admin/notifications", notification);

            System.out.println("[NotificationService] 신규가입 알림 전송 완료 - username: " + username);
        } catch (Exception e) {
            System.err.println("[NotificationService] 신규가입 알림 전송 실패: " + e.getMessage());
        }
    }

    @Override
    public void notifyReportBoard(int boardId, String boardTitle, String reporterUsername) {
        try {
            // 알림 데이터 생성
            NotificationVO notification = new NotificationVO();
            notification.setUser_id(null);
            notification.setAdmin_id(1); // 관리자 ID (임시로 1 설정)
            notification.setNotification_type("REPORT_BOARD");
            notification.setNotification_title("게시글 신고");
            notification.setNotification_message("'" + boardTitle + "' 게시글이 " + reporterUsername + "님에 의해 신고되었습니다.");
            notification.setIs_read(false);

            // DB에 저장
            notificationMapper.insertNotification(notification);

            // WebSocket으로 관리자에게 브로드캐스트 전송
            messagingTemplate.convertAndSend("/topic/admin/notifications", notification);

            System.out.println("[NotificationService] 게시글 신고 알림 전송 완료 - boardId: " + boardId);
        } catch (Exception e) {
            System.err.println("[NotificationService] 게시글 신고 알림 전송 실패: " + e.getMessage());
        }
    }

    @Override
    public void notifyReportComment(int commentId, String commentContent, String reporterUsername) {
        try {
            // 알림 데이터 생성
            NotificationVO notification = new NotificationVO();
            notification.setUser_id(null);
            notification.setAdmin_id(1); // 관리자 ID (임시로 1 설정)
            notification.setNotification_type("REPORT_COMMENT");
            notification.setNotification_title("댓글 신고");
            notification.setNotification_message("댓글이 " + reporterUsername + "님에 의해 신고되었습니다. 내용: " + 
                (commentContent.length() > 50 ? commentContent.substring(0, 50) + "..." : commentContent));
            notification.setIs_read(false);

            // DB에 저장
            notificationMapper.insertNotification(notification);

            // WebSocket으로 관리자에게 브로드캐스트 전송
            messagingTemplate.convertAndSend("/topic/admin/notifications", notification);

            System.out.println("[NotificationService] 댓글 신고 알림 전송 완료 - commentId: " + commentId);
        } catch (Exception e) {
            System.err.println("[NotificationService] 댓글 신고 알림 전송 실패: " + e.getMessage());
        }
    }

    @Override
    public void notifyInquiry(int inquiryId, String inquiryTitle, String inquirerUsername) {
        try {
            // 알림 데이터 생성
            NotificationVO notification = new NotificationVO();
            notification.setUser_id(null);
            notification.setAdmin_id(1); // 관리자 ID (임시로 1 설정)
            notification.setNotification_type("INQUIRY");
            notification.setNotification_title("문의사항 등록");
            notification.setNotification_message("'" + inquiryTitle + "' 문의사항이 " + inquirerUsername + "님에 의해 등록되었습니다.");
            notification.setIs_read(false);

            // DB에 저장
            notificationMapper.insertNotification(notification);

            // WebSocket으로 관리자에게 브로드캐스트 전송
            messagingTemplate.convertAndSend("/topic/admin/notifications", notification);

            System.out.println("[NotificationService] 문의사항 알림 전송 완료 - inquiryId: " + inquiryId);
        } catch (Exception e) {
            System.err.println("[NotificationService] 문의사항 알림 전송 실패: " + e.getMessage());
        }
    }

    // 알림 조회
    @Override
    public List<NotificationVO> getNotificationsByUserId(int userId) {
        System.out.println("[NotificationServiceImpl] 사용자 알림 조회 시작 - userId: " + userId);
        List<NotificationVO> notifications = notificationMapper.getNotificationsByUserId(userId);
        System.out.println("[NotificationServiceImpl] 매퍼에서 조회된 알림 개수: " + (notifications != null ? notifications.size() : "null"));
        if (notifications != null && !notifications.isEmpty()) {
            System.out.println("[NotificationServiceImpl] 첫 번째 알림 상세: " + notifications.get(0));
        } else {
            System.out.println("[NotificationServiceImpl] 매퍼에서 알림 데이터가 없습니다.");
        }
        return notifications;
    }

    @Override
    public List<NotificationVO> getAdminNotifications() {
        return notificationMapper.getAdminNotifications();
    }

    // 알림 읽음 처리
    @Override
    public void markAsRead(Long notificationId) {
        notificationMapper.markAsRead(notificationId);
    }

    // 디버깅용: 모든 알림 조회
    public List<NotificationVO> getAllNotifications() {
        System.out.println("[NotificationServiceImpl] 모든 알림 조회 시작");
        List<NotificationVO> allNotifications = notificationMapper.getAllNotifications();
        System.out.println("[NotificationServiceImpl] 전체 알림 개수: " + (allNotifications != null ? allNotifications.size() : "null"));
        return allNotifications;
    }
}
