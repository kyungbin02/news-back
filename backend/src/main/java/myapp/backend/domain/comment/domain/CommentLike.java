package myapp.backend.domain.comment.domain;

import java.time.LocalDateTime;

public class CommentLike {
    private Integer commentlikeId;
    private Integer commentId;
    private Integer userId;
    private LocalDateTime createdAt;

    public Integer getCommentlikeId() { return commentlikeId; }
    public void setCommentlikeId(Integer commentlikeId) { this.commentlikeId = commentlikeId; }
    public Integer getCommentId() { return commentId; }
    public void setCommentId(Integer commentId) { this.commentId = commentId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

