package myapp.backend.domain.comment.service.impl;

import myapp.backend.domain.comment.domain.Comment;
import myapp.backend.domain.comment.mapper.CommentMapper;
import myapp.backend.domain.comment.mapper.CommentLikeMapper;
import myapp.backend.domain.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    public List<Comment> getCommentsByNewsId(Integer newsId) {
        return commentMapper.findByNewsId(newsId);
    }

    @Override
    public List<Comment> getRepliesByParentId(Integer parentId) {
        return commentMapper.findRepliesByParentId(parentId);
    }

    @Override
    public Comment createComment(Comment comment) {
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        // 댓글 작성 후 사용자명을 포함한 완전한 정보를 조회하여 반환
        return commentMapper.findById(comment.getCommentId());
    }

    @Override
    public boolean updateComment(Integer commentId, String content, Integer userId) {
        Comment found = commentMapper.findById(commentId);
        if (found == null || !found.getUserId().equals(userId)) {
            return false;
        }
        found.setContent(content);
        return commentMapper.update(found) > 0;
    }

    @Override
    public boolean deleteComment(Integer commentId, Integer userId) {
        Comment found = commentMapper.findById(commentId);
        if (found == null || !found.getUserId().equals(userId)) {
            return false;
        }
        return commentMapper.delete(commentId, userId) > 0;
    }

    @Override
    public boolean toggleLike(Integer commentId, Integer userId) {
        if (commentLikeMapper.exists(commentId, userId)) {
            return commentLikeMapper.delete(commentId, userId) > 0;
        }
        myapp.backend.domain.comment.domain.CommentLike like = new myapp.backend.domain.comment.domain.CommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        like.setCreatedAt(LocalDateTime.now());
        return commentLikeMapper.insert(like) > 0;
    }

    @Override
    public int getLikeCount(Integer commentId) {
        return commentLikeMapper.countByCommentId(commentId);
    }
    
    // <경빈> 사용자별 댓글 조회 구현
    @Override
    public List<Comment> getCommentsByUserId(Integer userId) {
        try {
            System.out.println("=== 사용자별 댓글 조회 시작 ===");
            System.out.println("조회할 userId: " + userId);
            
            List<Comment> comments = commentMapper.findByUserIdWithNewsInfo(userId);
            System.out.println("조회된 댓글 수: " + (comments != null ? comments.size() : 0));
            
            if (comments != null && !comments.isEmpty()) {
                System.out.println("첫 번째 댓글 정보: " + comments.get(0));
            }
            
            return comments;
        } catch (Exception e) {
            System.out.println("사용자별 댓글 조회 오류: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
