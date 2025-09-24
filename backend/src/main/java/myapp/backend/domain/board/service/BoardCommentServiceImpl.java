package myapp.backend.domain.board.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import myapp.backend.domain.board.mapper.BoardCommentMapper;
import myapp.backend.domain.board.mapper.BoardMapper;
import myapp.backend.domain.board.vo.BoardCommentVO;
import myapp.backend.domain.board.vo.BoardVO;
import myapp.backend.domain.auth.mapper.UserMapper;
import myapp.backend.domain.notification.service.NotificationService;

@Service
public class BoardCommentServiceImpl implements BoardCommentService {
    
    @Autowired
    private BoardCommentMapper boardCommentMapper;
    
    @Autowired
    private BoardMapper boardMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public void createComment(int board_id, String comment_content, Integer parent_id, int user_id) {
        // 댓글 내용 검증
        if (comment_content == null || comment_content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용을 입력해주세요.");
        }
        
        // parent_id가 있는 경우 (대댓글), 해당 댓글이 존재하는지 확인
        if (parent_id != null) {
            BoardCommentVO parentComment = boardCommentMapper.getCommentsByBoardId(board_id).stream()
                .filter(comment -> comment.getComment_id() == parent_id)
                .findFirst()
                .orElse(null);
            
            if (parentComment == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 댓글입니다.");
            }
        }
        
        // 댓글 생성
        BoardCommentVO comment = new BoardCommentVO(user_id, board_id, comment_content, parent_id);
        boardCommentMapper.insertComment(comment);
        
        System.out.println("[BoardCommentServiceImpl] 댓글 작성 완료 - boardId: " + board_id + ", userId: " + user_id);
        
        // 게시글 댓글 알림 전송
        System.out.println("[BoardCommentServiceImpl] 알림 전송 시작 - boardId: " + board_id);
        try {
            BoardVO board = boardMapper.getBoardDetailById(board_id);
            System.out.println("[BoardCommentServiceImpl] 게시글 조회 결과 - board: " + (board != null ? "존재" : "없음"));
            
            if (board != null) {
                System.out.println("[BoardCommentServiceImpl] 게시글 작성자 ID: " + board.getUser_id() + ", 댓글 작성자 ID: " + user_id);
                
                if (board.getUser_id() != user_id) {
                    // 자신의 게시글이 아닌 경우에만 알림 전송
                    System.out.println("[BoardCommentServiceImpl] 다른 사용자 게시글 - 알림 전송 진행");
                    
                    String commenterUsername = userMapper.findByUserId(user_id).getUsername();
                    System.out.println("[BoardCommentServiceImpl] 댓글 작성자 이름: " + commenterUsername);
                    
                    String boardTitle = board.getTitle();
                    if (boardTitle == null || boardTitle.isEmpty()) {
                        boardTitle = "게시글";
                    }
                    System.out.println("[BoardCommentServiceImpl] 게시글 제목: " + boardTitle);
                    
                    notificationService.notifyBoardComment(
                        board_id, 
                        boardTitle, 
                        commenterUsername, 
                        board.getUser_id()
                    );
                    System.out.println("[BoardCommentServiceImpl] 게시글 댓글 알림 전송 완료 - boardId: " + board_id);
                } else {
                    System.out.println("[BoardCommentServiceImpl] 자신의 게시글 - 알림 전송 안함");
                }
            } else {
                System.out.println("[BoardCommentServiceImpl] 게시글을 찾을 수 없음 - boardId: " + board_id);
            }
        } catch (Exception e) {
            System.err.println("[BoardCommentServiceImpl] 게시글 댓글 알림 전송 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public List<BoardCommentVO> getCommentsByBoardId(int board_id) {
        return boardCommentMapper.getCommentsByBoardId(board_id);
    }
    
    @Override
    public List<BoardCommentVO> getRepliesByParentId(int parent_id) {
        return boardCommentMapper.getRepliesByParentId(parent_id);
    }
    
    @Override
    public void updateComment(int comment_id, String comment_content, int user_id) {
        // 댓글 내용 검증
        if (comment_content == null || comment_content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용을 입력해주세요.");
        }
        
        // 댓글 작성자 확인
        Integer authorUserId = boardCommentMapper.findCommentAuthorUserId(comment_id);
        if (authorUserId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다.");
        }
        
        if (!authorUserId.equals(user_id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 작성자만 수정할 수 있습니다.");
        }
        
        // 댓글 수정
        BoardCommentVO comment = new BoardCommentVO();
        comment.setComment_id(comment_id);
        comment.setComment_content(comment_content);
        comment.setUser_id(user_id);
        
        int updated = boardCommentMapper.updateComment(comment);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 수정에 실패했습니다.");
        }
        
        System.out.println("[BoardCommentServiceImpl] 댓글 수정 완료 - commentId: " + comment_id);
    }
    
    @Override
    public void deleteComment(int comment_id, int user_id) {
        // 댓글 작성자 확인
        Integer authorUserId = boardCommentMapper.findCommentAuthorUserId(comment_id);
        if (authorUserId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다.");
        }
        
        if (!authorUserId.equals(user_id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 작성자만 삭제할 수 있습니다.");
        }
        
        // 댓글 삭제
        int deleted = boardCommentMapper.deleteComment(comment_id);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 삭제에 실패했습니다.");
        }
        
        System.out.println("[BoardCommentServiceImpl] 댓글 삭제 완료 - commentId: " + comment_id);
    }
    
    @Override
    public int getCommentCountByBoardId(int board_id) {
        return boardCommentMapper.getCommentCountByBoardId(board_id);
    }
    
    // 대댓글 관련 메서드 구현
    
    @Override
    public void createReply(int board_id, int parent_id, String comment_content, int user_id) {
        // 대댓글 내용 검증
        if (comment_content == null || comment_content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대댓글 내용을 입력해주세요.");
        }
        
        // 부모 댓글이 존재하는지 확인
        BoardCommentVO parentComment = boardCommentMapper.getCommentsByBoardId(board_id).stream()
            .filter(comment -> comment.getComment_id() == parent_id)
            .findFirst()
            .orElse(null);
        
        if (parentComment == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 댓글입니다.");
        }
        
        // 부모 댓글이 이미 대댓글인지 확인 (대댓글에 대댓글 금지)
        if (parentComment.getParent_id() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대댓글에는 대댓글을 달 수 없습니다.");
        }
        
        // 대댓글 생성
        BoardCommentVO reply = new BoardCommentVO(user_id, board_id, comment_content, parent_id);
        boardCommentMapper.insertReply(reply);
        
        System.out.println("[BoardCommentServiceImpl] 대댓글 작성 완료 - boardId: " + board_id + ", parentId: " + parent_id + ", userId: " + user_id);
    }
    
    @Override
    public List<BoardCommentVO> getCommentsWithRepliesByBoardId(int board_id) {
        return boardCommentMapper.getCommentsWithRepliesByBoardId(board_id);
    }
    
    @Override
    public void updateReply(int comment_id, String comment_content, int user_id) {
        // 대댓글 내용 검증
        if (comment_content == null || comment_content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대댓글 내용을 입력해주세요.");
        }
        
        // 대댓글 작성자 확인
        Integer authorUserId = boardCommentMapper.findReplyAuthorUserId(comment_id);
        if (authorUserId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 대댓글입니다.");
        }
        
        if (!authorUserId.equals(user_id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "대댓글 작성자만 수정할 수 있습니다.");
        }
        
        // 대댓글 수정
        BoardCommentVO reply = new BoardCommentVO();
        reply.setComment_id(comment_id);
        reply.setComment_content(comment_content);
        reply.setUser_id(user_id);
        
        int updated = boardCommentMapper.updateReply(reply);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "대댓글 수정에 실패했습니다.");
        }
        
        System.out.println("[BoardCommentServiceImpl] 대댓글 수정 완료 - commentId: " + comment_id);
    }
    
    @Override
    public void deleteReply(int comment_id, int user_id) {
        // 대댓글 작성자 확인
        Integer authorUserId = boardCommentMapper.findReplyAuthorUserId(comment_id);
        if (authorUserId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 대댓글입니다.");
        }
        
        if (!authorUserId.equals(user_id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "대댓글 작성자만 삭제할 수 있습니다.");
        }
        
        // 대댓글 삭제
        int deleted = boardCommentMapper.deleteReply(comment_id);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "대댓글 삭제에 실패했습니다.");
        }
        
        System.out.println("[BoardCommentServiceImpl] 대댓글 삭제 완료 - commentId: " + comment_id);
    }
    
    @Override
    public void deleteCommentWithReplies(int comment_id, int user_id) {
        // 댓글 작성자 확인
        Integer authorUserId = boardCommentMapper.findCommentAuthorUserId(comment_id);
        if (authorUserId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다.");
        }
        
        if (!authorUserId.equals(user_id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 작성자만 삭제할 수 있습니다.");
        }
        
        // 댓글과 대댓글 모두 삭제
        int deleted = boardCommentMapper.deleteCommentWithReplies(comment_id);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "댓글 삭제에 실패했습니다.");
        }
        
        System.out.println("[BoardCommentServiceImpl] 댓글과 대댓글 모두 삭제 완료 - commentId: " + comment_id);
    }
    
    @Override
    public int getTotalCommentCountByBoardId(int board_id) {
        return boardCommentMapper.getTotalCommentCountByBoardId(board_id);
    }
    
    @Override
    public int getTopLevelCommentCountByBoardId(int board_id) {
        return boardCommentMapper.getTopLevelCommentCountByBoardId(board_id);
    }
}
