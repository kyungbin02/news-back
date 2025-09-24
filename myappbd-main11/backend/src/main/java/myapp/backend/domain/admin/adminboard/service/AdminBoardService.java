package myapp.backend.domain.admin.adminboard.service;

import java.util.List;

import myapp.backend.domain.admin.adminboard.vo.AdminBoardVO;

public interface AdminBoardService {
    
    // 관리자용 게시글 목록 조회
    List<AdminBoardVO> getAdminBoardList();
    
    // 관리자용 게시글 상세 조회
    AdminBoardVO getAdminBoardDetail(int board_id);
    
    // 관리자용 게시글 삭제
    boolean deleteAdminBoard(int board_id);
}
