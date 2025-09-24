package myapp.backend.domain.comment.controller;

import myapp.backend.domain.auth.vo.UserPrincipal;
import myapp.backend.domain.comment.domain.Comment;
import myapp.backend.domain.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "http://localhost:3000")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // 요청 DTO
    public static class CreateCommentRequest {
        public Integer newsId;
        public String content;
        public Integer parentId;
    }

    // 댓글 목록 조회 - 가장 기본적인 방식
    @GetMapping("/news/{newsId}")
    public ResponseEntity<Map<String, Object>> getComments(@PathVariable("newsId") String newsId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer newsIdInt = Integer.parseInt(newsId);
            List<Comment> comments = commentService.getCommentsByNewsId(newsIdInt);
            
            response.put("success", true);
            response.put("data", comments);
            response.put("total", comments.size());
            
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("error", "잘못된 뉴스 ID: " + newsId);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "댓글 조회 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 대댓글 조회
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Map<String, Object>> getReplies(@PathVariable("commentId") Integer commentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Comment> replies = commentService.getRepliesByParentId(commentId);
            response.put("success", true);
            response.put("data", replies);
            response.put("total", replies.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "대댓글 조회 중 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 댓글 작성
    @PostMapping
    public ResponseEntity<Map<String, Object>> createComment(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserPrincipal user,
            @RequestBody CreateCommentRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        
        if (user == null) {
            response.put("success", false);
            response.put("error", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }
        
        if (request == null || request.newsId == null || request.content == null || request.content.trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "필수 정보가 누락되었습니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            Comment comment = new Comment();
            comment.setUserId(user.getUserId());
            comment.setNewsId(request.newsId);
            comment.setContent(request.content.trim());
            comment.setParentId(request.parentId);
            
            Comment created = commentService.createComment(comment);
            response.put("success", true);
            response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "댓글 작성 중 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> updateComment(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserPrincipal user,
            @PathVariable("commentId") Integer commentId,
            @RequestBody Map<String, String> body
    ) {
        Map<String, Object> response = new HashMap<>();
        
        if (user == null) {
            response.put("success", false);
            response.put("error", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }
        
        String content = body != null ? body.get("content") : null;
        if (content == null || content.trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "댓글 내용이 필요합니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            boolean success = commentService.updateComment(commentId, content.trim(), user.getUserId());
            response.put("success", success);
            if (!success) {
                response.put("error", "권한이 없거나 댓글이 존재하지 않습니다.");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "댓글 수정 중 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserPrincipal user,
            @PathVariable("commentId") Integer commentId
    ) {
        Map<String, Object> response = new HashMap<>();
        
        if (user == null) {
            response.put("success", false);
            response.put("error", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            boolean success = commentService.deleteComment(commentId, user.getUserId());
            response.put("success", success);
            if (!success) {
                response.put("error", "권한이 없거나 댓글이 존재하지 않습니다.");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "댓글 삭제 중 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 좋아요 토글
    @PostMapping("/{commentId}/reaction")
    public ResponseEntity<Map<String, Object>> toggleReaction(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserPrincipal user,
            @PathVariable("commentId") Integer commentId
    ) {
        Map<String, Object> response = new HashMap<>();
        
        if (user == null) {
            response.put("success", false);
            response.put("error", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            boolean success = commentService.toggleLike(commentId, user.getUserId());
            response.put("success", success);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "좋아요 처리 중 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 좋아요 수 조회
    @GetMapping("/{commentId}/likes")
    public ResponseEntity<Map<String, Object>> getLikeCount(@PathVariable("commentId") Integer commentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int count = commentService.getLikeCount(commentId);
            response.put("success", true);
            response.put("likeCount", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "좋아요 수 조회 중 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // <경빈> 마이페이지용 사용자 댓글 조회 API
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyComments(
            @org.springframework.security.core.annotation.AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            System.out.println("=== 마이페이지 댓글 조회 API 호출 ===");
            System.out.println("userPrincipal: " + (userPrincipal != null ? "인증됨" : "null"));

            if (userPrincipal == null) {
                System.out.println("인증 실패: userPrincipal이 null");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(errorResponse);
            }

            Integer userId = userPrincipal.getUserId();
            System.out.println("userId: " + userId);

            // 사용자별 댓글 조회
            List<Comment> comments = commentService.getCommentsByUserId(userId);
            System.out.println("조회된 댓글 수: " + (comments != null ? comments.size() : 0));

            // 프론트엔드 요구사항에 맞는 응답 형식으로 변환
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", comments);
            response.put("message", "사용자 댓글 조회 성공");

            System.out.println("응답 데이터: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("사용자 댓글 조회 오류: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "댓글 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
