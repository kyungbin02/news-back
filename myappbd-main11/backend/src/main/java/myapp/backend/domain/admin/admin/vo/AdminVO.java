package myapp.backend.domain.admin.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminVO {
    private int admin_id;
    private int user_id;
    private String admin_level;
    private LocalDateTime created_at;
    // join
    private String username;
}
