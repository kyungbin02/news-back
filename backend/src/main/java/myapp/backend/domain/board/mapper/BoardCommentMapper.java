package myapp.backend.domain.board.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import myapp.backend.domain.board.vo.BoardCommentVO;

@Mapper
public interface BoardCommentMapper {
    // 댓글 작성
    void insertComment(BoardCommentVO comment);
    
    // 게시글의 댓글 목록 조회 (최상위 댓글만)
    List<BoardCommentVO> getCommentsByBoardId(int board_id);
    
    // 특정 댓글의 대댓글 목록 조회
    List<BoardCommentVO> getRepliesByParentId(int parent_id);
    
    // 댓글 수정
    int updateComment(BoardCommentVO comment);
    
    // 댓글 삭제
    int deleteComment(int comment_id);
    
    // 댓글 작성자 확인
    Integer findCommentAuthorUserId(int comment_id);
    
    // 댓글 ID로 댓글 조회
    BoardCommentVO getCommentById(int comment_id);
    
    // 게시글의 총 댓글 수 조회
    int getCommentCountByBoardId(int board_id);
    
    // 대댓글 관련 메서드
    void insertReply(BoardCommentVO reply); // 대댓글 작성
    List<BoardCommentVO> getCommentsWithRepliesByBoardId(int board_id); // 게시글의 모든 댓글과 대댓글을 계층 구조로 조회
    int updateReply(BoardCommentVO reply); // 대댓글 수정
    int deleteReply(int comment_id); // 대댓글 삭제
    int deleteCommentWithReplies(int comment_id); // 댓글과 대댓글 모두 삭제 (계층 삭제)
    Integer findReplyAuthorUserId(int comment_id); // 대댓글 작성자 확인
    int getTotalCommentCountByBoardId(int board_id); // 게시글의 총 댓글 수 (대댓글 포함)
    int getTopLevelCommentCountByBoardId(int board_id); // 게시글의 최상위 댓글 수 (대댓글 제외)
}
