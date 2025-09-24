package myapp.backend.domain.mynews.mapper;

import myapp.backend.domain.mynews.domain.Bookmark;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BookmarkMapper {
    
    @Insert("INSERT INTO bookmark (user_id, news_id, created_at) VALUES (#{userId}, #{newsId}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "bookmarkId")
    int insertBookmark(Bookmark bookmark);
    
    @Delete("DELETE FROM bookmark WHERE bookmark_id = #{bookmarkId} AND user_id = #{userId}")
    int deleteBookmark(@Param("bookmarkId") Integer bookmarkId, @Param("userId") Integer userId);
    
    @Delete("DELETE FROM bookmark WHERE user_id = #{userId} AND news_id = #{newsId}")
    int deleteBookmarkByNewsId(@Param("userId") Integer userId, @Param("newsId") Integer newsId);
    
    @Select("SELECT b.bookmark_id as bookmarkId, b.user_id as userId, b.news_id as newsId, b.created_at as createdAt, " +
            "n.title as newsTitle, n.content as newsContent, n.image_url as imageUrl, n.category, n.created_at as newsCreatedAt " +
            "FROM bookmark b " +
            "LEFT JOIN news n ON b.news_id = n.news_id " +
            "WHERE b.user_id = #{userId} " +
            "ORDER BY b.created_at DESC")
    List<Bookmark> findByUserId(@Param("userId") Integer userId);
    
    @Select("SELECT b.bookmark_id as bookmarkId, b.user_id as userId, b.news_id as newsId, b.created_at as createdAt, " +
            "n.title as newsTitle, n.content as newsContent, n.image_url as imageUrl, n.category, n.created_at as newsCreatedAt " +
            "FROM bookmark b " +
            "LEFT JOIN news n ON b.news_id = n.news_id " +
            "WHERE b.user_id = #{userId} AND b.news_id = #{newsId}")
    Bookmark findByUserIdAndNewsId(@Param("userId") Integer userId, @Param("newsId") Integer newsId);
    
    @Select("SELECT COUNT(*) FROM bookmark WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Integer userId);
}






