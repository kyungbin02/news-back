package myapp.backend.domain.comment.mapper;

import myapp.backend.domain.comment.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> findByNewsId(@Param("newsId") Integer newsId);
    List<Comment> findRepliesByParentId(@Param("parentId") Integer parentId);
    int insert(Comment comment);
    Comment findById(@Param("commentId") Integer commentId);
    int update(Comment comment);
    int delete(@Param("commentId") Integer commentId, @Param("userId") Integer userId);
    
    // <경빈> 사용자별 댓글 조회 (뉴스 정보 포함)
    List<Comment> findByUserIdWithNewsInfo(@Param("userId") Integer userId);
}
