package myapp.backend.domain.mynews.mapper;

import myapp.backend.domain.mynews.domain.ViewHistory;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ViewHistoryMapper {
    
    @Insert("INSERT INTO view_history (user_id, news_id, viewed_at, read_time) VALUES (#{userId}, #{newsId}, CURRENT_TIMESTAMP, #{readTime})")
    @Options(useGeneratedKeys = true, keyProperty = "viewId")
    int insertViewHistory(ViewHistory viewHistory);
    
    @Update("UPDATE view_history SET read_time = #{readTime} WHERE view_id = #{viewId} AND user_id = #{userId}")
    int updateReadTime(@Param("viewId") Integer viewId, @Param("userId") Integer userId, @Param("readTime") Integer readTime);
    
    @Select("SELECT v.view_id as viewId, v.user_id as userId, v.news_id as newsId, v.viewed_at as viewedAt, v.read_time as readTime, " +
            "n.title as newsTitle, n.content as newsContent, n.image_url as imageUrl, n.category, n.created_at as newsCreatedAt " +
            "FROM view_history v " +
            "LEFT JOIN news n ON v.news_id = n.news_id " +
            "WHERE v.user_id = #{userId} " +
            "ORDER BY v.viewed_at DESC " +
            "LIMIT #{limit}")
    List<ViewHistory> findByUserIdWithLimit(@Param("userId") Integer userId, @Param("limit") int limit);
    
    @Select("SELECT v.view_id as viewId, v.user_id as userId, v.news_id as newsId, v.viewed_at as viewedAt, v.read_time as readTime, " +
            "n.title as newsTitle, n.content as newsContent, n.image_url as imageUrl, n.category, n.created_at as newsCreatedAt " +
            "FROM view_history v " +
            "LEFT JOIN news n ON v.news_id = n.news_id " +
            "WHERE v.user_id = #{userId} " +
            "ORDER BY v.viewed_at DESC")
    List<ViewHistory> findByUserId(@Param("userId") Integer userId);
    
    @Select("SELECT v.view_id as viewId, v.user_id as userId, v.news_id as newsId, v.viewed_at as viewedAt, v.read_time as readTime, " +
            "n.title as newsTitle, n.content as newsContent, n.image_url as imageUrl, n.category, n.created_at as newsCreatedAt " +
            "FROM view_history v " +
            "LEFT JOIN news n ON v.news_id = n.news_id " +
            "WHERE v.user_id = #{userId} AND v.news_id = #{newsId} " +
            "ORDER BY v.viewed_at DESC " +
            "LIMIT 1")
    ViewHistory findLatestByUserIdAndNewsId(@Param("userId") Integer userId, @Param("newsId") Integer newsId);
    
    @Select("SELECT COUNT(*) FROM view_history WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Integer userId);
    
    @Select("SELECT SUM(read_time) FROM view_history WHERE user_id = #{userId}")
    Integer getTotalReadTimeByUserId(@Param("userId") Integer userId);
}






