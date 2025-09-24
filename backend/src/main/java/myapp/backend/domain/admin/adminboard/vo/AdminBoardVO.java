package myapp.backend.domain.admin.adminboard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminBoardVO {
    private int board_id;
    private String board_content;
    private LocalDateTime uploaded_at;
    private int view;
    
    // JOIN을 위한 필드
    private int user_id;
    private int image_id;
    private String username;
    private String image_url;
    private List<String> imageUrls;
    
    // 관리자용 필드
    private Integer reportCount; // 신고 수
    private Boolean isReported; // 신고 상태 (신고가 있으면 true)
    
    // board_content에서 제목과 내용을 분리하는 메서드
    public String getTitle() {
        if (board_content != null && board_content.startsWith("[") && board_content.contains("]")) {
            int endIndex = board_content.indexOf("]");
            if (endIndex > 1) {
                return board_content.substring(1, endIndex);
            }
        }
        return "";
    }
    
    public String getContent() {
        if (board_content != null && board_content.startsWith("[") && board_content.contains("]")) {
            int endIndex = board_content.indexOf("]");
            if (endIndex > 1) {
                return board_content.substring(endIndex + 1).trim();
            }
        }
        return board_content;
    }
}
