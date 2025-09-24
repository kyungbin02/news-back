package myapp.backend.domain.board.service;

import java.util.List;
import myapp.backend.domain.board.vo.BoardCommentVO;

public interface BoardCommentService {
    // 댓글 작성
    void createComment(int board_id, String comment_content, Integer parent_id, int user_id);
    
    // 게시글의 댓글 목록 조회 (최상위 댓글만)
    List<BoardCommentVO> getCommentsByBoardId(int board_id);
    
    // 특정 댓글의 대댓글 목록 조회
    List<BoardCommentVO> getRepliesByParentId(int parent_id);
    
    // 댓글 수정
    void updateComment(int comment_id, String comment_content, int user_id);
    
    // 댓글 삭제
    void deleteComment(int comment_id, int user_id);
    
    // 게시글의 총 댓글 수 조회
    int getCommentCountByBoardId(int board_id);
    
    // 대댓글 관련 메서드
    void createReply(int board_id, int parent_id, String comment_content, int user_id); // 대댓글 작성
    List<BoardCommentVO> getCommentsWithRepliesByBoardId(int board_id); // 게시글의 모든 댓글과 대댓글을 계층 구조로 조회
    void updateReply(int comment_id, String comment_content, int user_id); // 대댓글 수정
    void deleteReply(int comment_id, int user_id); // 대댓글 삭제
    void deleteCommentWithReplies(int comment_id, int user_id); // 댓글과 대댓글 모두 삭제 (계층 삭제)
    int getTotalCommentCountByBoardId(int board_id); // 게시글의 총 댓글 수 (대댓글 포함)
    int getTopLevelCommentCountByBoardId(int board_id); // 게시글의 최상위 댓글 수 (대댓글 제외)
}
