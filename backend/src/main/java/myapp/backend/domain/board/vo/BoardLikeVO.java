package myapp.backend.domain.board.vo;

public class BoardLikeVO {
    private int likes_id;
    private int user_id;
    private int board_id;
    
    // 기본 생성자
    public BoardLikeVO() {}
    
    // 생성자
    public BoardLikeVO(int user_id, int board_id) {
        this.user_id = user_id;
        this.board_id = board_id;
    }
    
    // Getter와 Setter
    public int getLikes_id() {
        return likes_id;
    }
    
    public void setLikes_id(int likes_id) {
        this.likes_id = likes_id;
    }
    
    public int getUser_id() {
        return user_id;
    }
    
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
    
    public int getBoard_id() {
        return board_id;
    }
    
    public void setBoard_id(int board_id) {
        this.board_id = board_id;
    }
}

