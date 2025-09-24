package myapp.backend.domain.comment.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Comment {
    private Integer commentId;
    private Integer userId;
    private Integer newsId;
    private String content;
    private Integer parentId;
    private LocalDateTime createdAt;
    private String userName; // 사용자 이름 추가
    private List<Comment> replies; // 대댓글 목록
    private Integer replyCount; // 대댓글 수
    private String newsTitle; // <경빈> 뉴스 제목 추가
    private Integer likeCount; // <경빈> 좋아요 수 추가

    public Comment() {}

    public Comment(Integer userId, Integer newsId, String content, Integer parentId) {
        this.userId = userId;
        this.newsId = newsId;
        this.content = content;
        this.parentId = parentId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Integer getCommentId() { return commentId; }
    public void setCommentId(Integer commentId) { this.commentId = commentId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getNewsId() { return newsId; }
    public void setNewsId(Integer newsId) { this.newsId = newsId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUserName() { return userName; } // 사용자 이름 getter
    public void setUserName(String userName) { this.userName = userName; } // 사용자 이름 setter
    public List<Comment> getReplies() { return replies; } // 대댓글 목록 getter
    public void setReplies(List<Comment> replies) { this.replies = replies; } // 대댓글 목록 setter
    public Integer getReplyCount() { return replyCount; } // 대댓글 수 getter
    public void setReplyCount(Integer replyCount) { this.replyCount = replyCount; } // 대댓글 수 setter
    public String getNewsTitle() { return newsTitle; } // <경빈> 뉴스 제목 getter
    public void setNewsTitle(String newsTitle) { this.newsTitle = newsTitle; } // <경빈> 뉴스 제목 setter
    public Integer getLikeCount() { return likeCount; } // <경빈> 좋아요 수 getter
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; } // <경빈> 좋아요 수 setter
}
