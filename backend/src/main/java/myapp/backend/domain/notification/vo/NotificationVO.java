package myapp.backend.domain.notification.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationVO {
    private Long id;
    private Integer user_id; // 사용자 ID (NULL이면 관리자용)
    private Integer admin_id; // 관리자 ID (NULL이면 사용자용)
    private String notification_type; // INQUIRY_ANSWER, BOARD_COMMENT, NEW_USER, REPORT_BOARD, REPORT_COMMENT, INQUIRY
    private String notification_title;
    private String notification_message;
    private Boolean is_read; // 읽음 여부 (기본값 FALSE)
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}



