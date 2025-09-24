package myapp.backend.domain.report.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SanctionRequest {
    private String action;          // 'reject', 'sanction'
    private String sanctionType;    // 'warning', 'suspended_7days'
    private String admin_comment;   // 관리자 코멘트 (프론트엔드와 일치)
}
