package myapp.backend.domain.comment.service;

import myapp.backend.domain.comment.domain.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> getCommentsByNewsId(Integer newsId);
    List<Comment> getRepliesByParentId(Integer parentId);
    Comment createComment(Comment comment);
    boolean updateComment(Integer commentId, String content, Integer userId);
    boolean deleteComment(Integer commentId, Integer userId);
    boolean toggleLike(Integer commentId, Integer userId);
    int getLikeCount(Integer commentId);
    
    // <경빈> 사용자별 댓글 조회
    List<Comment> getCommentsByUserId(Integer userId);
}
