package myapp.backend.domain.board.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageVO {
    private int image_id;      // images.image_id
    private String image_url;  // images.image_url
}
