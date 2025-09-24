package myapp.backend.domain.inquiry.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InquiryVO {
    private int inquiry_id;
    private int user_id;
    private String inquiry_title;
    private String inquiry_content;
    private String inquiry_status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    
    // JOIN을 위한 필드
    private String username; // 사용자 이름
    private String answer_content; // 답변 내용
    private LocalDateTime answer_created_at; // 답변 작성일
    private String admin_username; // 답변 관리자 이름
}
