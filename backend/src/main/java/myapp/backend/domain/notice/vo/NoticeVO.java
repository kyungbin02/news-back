package myapp.backend.domain.notice.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeVO {
    private int notice_id;
    private String notice_title;
    private String notice_content;
    private int admin_id;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private int is_important;  // TINYINT(1)과 호환을 위해 int로 변경
    private int view_count;
    
    // JOIN을 위한 필드
    private String admin_username; // 관리자 이름
}
