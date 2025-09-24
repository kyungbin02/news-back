package myapp.backend.domain.admin.adminboard.controller;

import myapp.backend.domain.admin.adminboard.service.AdminBoardService;
import myapp.backend.domain.admin.adminboard.vo.AdminBoardVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/board")
public class AdminBoardController {
    
    @Autowired
    private AdminBoardService adminBoardService;
    
    // 관리자용 게시글 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<AdminBoardVO>> getAdminBoardList(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            // TODO: 관리자 권한 확인 로직 추가
            List<AdminBoardVO> boardList = adminBoardService.getAdminBoardList();
            return ResponseEntity.ok(boardList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 관리자용 게시글 상세 조회
    @GetMapping("/{board_id}")
    public ResponseEntity<AdminBoardVO> getAdminBoardDetail(
            @PathVariable("board_id") int board_id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            // TODO: 관리자 권한 확인 로직 추가
            AdminBoardVO board = adminBoardService.getAdminBoardDetail(board_id);
            if (board != null) {
                return ResponseEntity.ok(board);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 관리자용 게시글 삭제
    @DeleteMapping("/{board_id}")
    public ResponseEntity<String> deleteAdminBoard(
            @PathVariable("board_id") int board_id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            // TODO: 관리자 권한 확인 로직 추가
            boolean result = adminBoardService.deleteAdminBoard(board_id);
            if (result) {
                return ResponseEntity.ok("게시글이 성공적으로 삭제되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("게시글 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }
}
