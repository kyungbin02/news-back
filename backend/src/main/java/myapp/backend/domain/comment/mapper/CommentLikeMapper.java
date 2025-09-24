package myapp.backend.domain.comment.mapper;

import myapp.backend.domain.comment.domain.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentLikeMapper {
    int insert(CommentLike like);
    int delete(@Param("commentId") Integer commentId, @Param("userId") Integer userId);
    boolean exists(@Param("commentId") Integer commentId, @Param("userId") Integer userId);
    int countByCommentId(@Param("commentId") Integer commentId);
}

