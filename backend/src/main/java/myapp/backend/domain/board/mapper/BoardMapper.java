package myapp.backend.domain.board.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import myapp.backend.domain.board.vo.BoardVO;
import myapp.backend.domain.board.vo.ImageVO;
import myapp.backend.domain.board.vo.BoardLikeVO;

@Mapper
public interface BoardMapper {
    List<BoardVO> getBoardList();
    void insertBoard(BoardVO board); // 게시물 작성
    void updateViewCount(int board_id); // 조회수 증가용
    BoardVO getBoardDetailById(int board_id); // 개별 글 조회
    Integer findAuthorUserId(int board_id); // 작성자 조회
    int deleteBoard(int board_id); // 게시물 삭제
    int updateBoard(BoardVO board); // 게시물 수정
    
    // 이미지 관련 메서드
    int insertImage(ImageVO imageVO); // 이미지 정보 저장
    void updateBoardImageId(@Param("board_id") int board_id, @Param("image_id") int image_id); // 게시글의 image_id 업데이트
    void deleteImage(int image_id); // 이미지 삭제
    

    
    // 최근 생성된 게시글 조회
    BoardVO getLatestBoardByUserId(int userId);
    
    // 좋아요 관련 메서드
    void insertBoardLike(BoardLikeVO boardLike); // 좋아요 추가
    void deleteBoardLike(@Param("user_id") int user_id, @Param("board_id") int board_id); // 좋아요 취소
    boolean existsBoardLike(@Param("user_id") int user_id, @Param("board_id") int board_id); // 좋아요 존재 여부 확인
    int getBoardLikeCount(int board_id); // 게시글 좋아요 수 조회
    
    // 인증된 사용자를 위한 게시글 목록 조회 (좋아요 상태 포함)
    List<BoardVO> getBoardListWithLikeStatus(int userId);


}
