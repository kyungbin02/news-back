package myapp.backend.domain.admin.adminboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import myapp.backend.domain.admin.adminboard.vo.AdminBoardVO;

import java.util.List;

@Mapper
public interface AdminBoardMapper {
    
    // 관리자용 게시글 목록 조회 (신고 정보 포함)
    List<AdminBoardVO> getAdminBoardList();
    
    // 관리자용 게시글 상세 조회 (신고 정보 포함)
    AdminBoardVO getAdminBoardDetail(@Param("board_id") int board_id);
    
    // 관리자용 게시글 삭제
    int deleteAdminBoard(@Param("board_id") int board_id);
    
    // 게시글 신고 수 조회
    Integer getBoardReportCount(@Param("board_id") int board_id);
    
    // 게시글 신고 상태 확인 (신고가 있으면 true)
    Boolean isBoardReported(@Param("board_id") int board_id);
}
