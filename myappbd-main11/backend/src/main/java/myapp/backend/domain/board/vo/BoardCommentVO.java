package myapp.backend.domain.board.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardCommentVO {
    private int comment_id;
    private int user_id;
    private int board_id;
    private String comment_content;
    private Integer parent_id; // 대댓글용 (null이면 최상위 댓글)
    private LocalDateTime uploaded_at;
    
    // JOIN을 위한 추가 필드
    private String username; // 작성자 이름
    private String user_profile_image; // 작성자 프로필 이미지 (필요시)
    
    // 생성자 (댓글 작성용)
    public BoardCommentVO(int user_id, int board_id, String comment_content, Integer parent_id) {
        this.user_id = user_id;
        this.board_id = board_id;
        this.comment_content = comment_content;
        this.parent_id = parent_id;
    }
}
