package myapp.backend.domain.report.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportVO {
    private int report_id;
    private int reporter_id;
    private int reported_user_id;
    private String report_reason;
    private String report_content;
    private String target_type;
    private Integer target_id;
    private String report_status;
    private Integer admin_id;
    private String admin_comment;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    
    // JOIN을 위한 필드
    private String reporter_username;    // 신고자 이름
    private String reported_username;    // 신고받은 사용자 이름
    private String admin_username;       // 관리자 이름
    
    // 신고된 내용 (target_type에 따라)
    private String board_content;        // 게시물 내용 (제목+내용)
    private String comment_content;      // 댓글 내용
}
