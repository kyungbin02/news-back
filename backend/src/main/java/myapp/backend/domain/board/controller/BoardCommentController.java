package myapp.backend.domain.board.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import myapp.backend.domain.board.service.BoardCommentService;
import myapp.backend.domain.board.vo.BoardCommentVO;
import myapp.backend.domain.auth.vo.UserPrincipal;

@RestController
@RequestMapping("/api/board/comment")
public class BoardCommentController {
    
    @Autowired
    private BoardCommentService boardCommentService;
    
    // 댓글 작성
    @PostMapping("/{board_id}")
    public ResponseEntity<?> createComment(
            @PathVariable("board_id") int board_id,
            @RequestParam("comment_content") String comment_content,
            @RequestParam(value = "parent_id", required = false) Integer parent_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        System.out.println("[BoardCommentController] 🗨️ 댓글 작성 요청 - boardId: " + board_id);
        System.out.println("[BoardCommentController] 🗨️ 댓글 내용: " + comment_content);
        System.out.println("[BoardCommentController] 🗨️ 부모 댓글 ID: " + parent_id);
        System.out.println("[BoardCommentController] 🗨️ 인증된 사용자: " + (principal != null ? "userId=" + principal.getUserId() + ", username=" + principal.getUsername() : "null"));
        
        if (principal == null) {
            System.err.println("[BoardCommentController] 🗨️ 댓글 작성 실패 - 인증되지 않은 사용자 ❌");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        try {
            boardCommentService.createComment(board_id, comment_content, parent_id, principal.getUserId());
            System.out.println("[BoardCommentController] 🗨️ 댓글 작성 성공 ✅");
            return ResponseEntity.ok().body("댓글이 성공적으로 작성되었습니다.");
        } catch (Exception e) {
            System.err.println("[BoardCommentController] 🗨️ 댓글 작성 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("댓글 작성에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 게시글의 댓글 목록 조회 (최상위 댓글만)
    @GetMapping("/{board_id}")
    public ResponseEntity<List<BoardCommentVO>> getCommentsByBoardId(@PathVariable("board_id") int board_id) {
        try {
            List<BoardCommentVO> comments = boardCommentService.getCommentsByBoardId(board_id);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 특정 댓글의 대댓글 목록 조회
    @GetMapping("/replies/{parent_id}")
    public ResponseEntity<List<BoardCommentVO>> getRepliesByParentId(@PathVariable("parent_id") int parent_id) {
        try {
            List<BoardCommentVO> replies = boardCommentService.getRepliesByParentId(parent_id);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 댓글 수정
    @PutMapping("/{comment_id}")
    public ResponseEntity<?> updateComment(
            @PathVariable("comment_id") int comment_id,
            @RequestParam("comment_content") String comment_content,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        try {
            boardCommentService.updateComment(comment_id, comment_content, principal.getUserId());
            return ResponseEntity.ok().body("댓글이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("댓글 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 댓글 삭제
    @DeleteMapping("/{comment_id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("comment_id") int comment_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        try {
            boardCommentService.deleteComment(comment_id, principal.getUserId());
            return ResponseEntity.ok().body("댓글이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("댓글 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 게시글의 총 댓글 수 조회
    @GetMapping("/{board_id}/count")
    public ResponseEntity<Map<String, Integer>> getCommentCountByBoardId(@PathVariable("board_id") int board_id) {
        try {
            int count = boardCommentService.getCommentCountByBoardId(board_id);
            return ResponseEntity.ok(Map.of("commentCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 대댓글 관련 엔드포인트
    
    // 대댓글 작성
    @PostMapping("/{board_id}/reply")
    public ResponseEntity<?> createReply(
            @PathVariable("board_id") int board_id,
            @RequestParam("parent_id") int parent_id,
            @RequestParam("comment_content") String comment_content,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        System.out.println("[BoardCommentController] 🗨️ 대댓글 작성 요청 - boardId: " + board_id + ", parentId: " + parent_id);
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        try {
            boardCommentService.createReply(board_id, parent_id, comment_content, principal.getUserId());
            System.out.println("[BoardCommentController] 🗨️ 대댓글 작성 성공 ✅");
            return ResponseEntity.ok().body("대댓글이 성공적으로 작성되었습니다.");
        } catch (Exception e) {
            System.out.println("[BoardCommentController] 🗨️ 대댓글 작성 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("대댓글 작성에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 게시글의 모든 댓글과 대댓글을 계층 구조로 조회
    @GetMapping("/{board_id}/hierarchy")
    public ResponseEntity<List<BoardCommentVO>> getCommentsWithRepliesByBoardId(@PathVariable("board_id") int board_id) {
        try {
            List<BoardCommentVO> comments = boardCommentService.getCommentsWithRepliesByBoardId(board_id);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 대댓글 수정
    @PutMapping("/reply/{comment_id}")
    public ResponseEntity<?> updateReply(
            @PathVariable("comment_id") int comment_id,
            @RequestParam("comment_content") String comment_content,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        try {
            boardCommentService.updateReply(comment_id, comment_content, principal.getUserId());
            return ResponseEntity.ok().body("대댓글이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("대댓글 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 대댓글 삭제
    @DeleteMapping("/reply/{comment_id}")
    public ResponseEntity<?> deleteReply(
            @PathVariable("comment_id") int comment_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        try {
            boardCommentService.deleteReply(comment_id, principal.getUserId());
            return ResponseEntity.ok().body("대댓글이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("대댓글 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 댓글과 대댓글 모두 삭제 (계층 삭제)
    @DeleteMapping("/{comment_id}/with-replies")
    public ResponseEntity<?> deleteCommentWithReplies(
            @PathVariable("comment_id") int comment_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        try {
            boardCommentService.deleteCommentWithReplies(comment_id, principal.getUserId());
            return ResponseEntity.ok().body("댓글과 대댓글이 모두 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("댓글 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 게시글의 총 댓글 수 (대댓글 포함)
    @GetMapping("/{board_id}/total-count")
    public ResponseEntity<Map<String, Integer>> getTotalCommentCountByBoardId(@PathVariable("board_id") int board_id) {
        try {
            int count = boardCommentService.getTotalCommentCountByBoardId(board_id);
            return ResponseEntity.ok(Map.of("totalCommentCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 게시글의 최상위 댓글 수 (대댓글 제외)
    @GetMapping("/{board_id}/top-level-count")
    public ResponseEntity<Map<String, Integer>> getTopLevelCommentCountByBoardId(@PathVariable("board_id") int board_id) {
        try {
            int count = boardCommentService.getTopLevelCommentCountByBoardId(board_id);
            return ResponseEntity.ok(Map.of("topLevelCommentCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
