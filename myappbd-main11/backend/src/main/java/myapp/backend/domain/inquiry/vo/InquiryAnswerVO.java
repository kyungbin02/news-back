package myapp.backend.domain.inquiry.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InquiryAnswerVO {
    private int answer_id;
    private int inquiry_id;
    private int admin_id;
    private String answer_content;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    
    // JOIN을 위한 필드
    private String admin_username; // 관리자 이름
}
