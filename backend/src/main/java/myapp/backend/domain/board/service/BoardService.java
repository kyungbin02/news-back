package myapp.backend.domain.board.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import myapp.backend.domain.board.vo.BoardVO;

public interface BoardService {
    List<BoardVO> getBoardList();
    void createBoard(BoardVO board); // 게시물 작성
    void createBoardWithImages(String content, MultipartFile[] images, int userId); // 이미지 첨부 게시물 작성
    BoardVO getBoard(int board_id); // 개별 글 조회
    void increaseViewCount(int board_id); // 조회수 증가
    BoardVO getBoardDetail(int board_id, Integer currentUserId); // 상세페이지 조회
    void deleteBoard(int board_id, int userId); // 게시물 삭제
    void updateBoard(int board_id, BoardVO updatedBoard, int userId); // 게시물 수정
    
    // 좋아요 관련 메서드
    boolean toggleBoardLike(int board_id, int userId); // 좋아요 토글 (추가/취소)
    boolean isLikedByUser(int board_id, int userId); // 사용자가 좋아요했는지 확인
    int getBoardLikeCount(int board_id); // 게시글 좋아요 수 조회
    void updateBoardWithImages(int boardId, String title, String content, MultipartFile[] images, int userId); // 게시물 수정 (이미지 포함)
    
    // 인증된 사용자를 위한 게시글 목록 조회 (좋아요 상태 포함)
    List<BoardVO> getBoardListWithLikeStatus(int userId);
    
    // 관리자용 게시글 관리 메서드

}