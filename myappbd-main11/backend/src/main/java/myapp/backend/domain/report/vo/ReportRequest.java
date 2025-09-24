package myapp.backend.domain.report.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {
    private int reported_user_id;    // 신고받을 사용자 ID
    private String report_reason;    // 신고 사유
    private String report_content;   // 신고 상세 내용 (선택사항)
    private String target_type;      // 신고 대상 타입 ('board', 'board_comment', 'comments', 'profile')
    private Integer target_id;       // 해당 게시물/댓글 ID (profile 신고 시 null)
}




