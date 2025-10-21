package myapp.backend.domain.search.mapper;

import myapp.backend.domain.search.domain.NewsClickTracking;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NewsClickTrackingMapper {

    /**
     * 새로운 뉴스 클릭 추적 삽입
     */
    @Insert("INSERT INTO news_click_tracking (news_id, news_title, click_count, last_clicked_at) " +
            "VALUES (#{newsId}, #{newsTitle}, #{clickCount}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "clickId")
    int insertNewsClickTracking(NewsClickTracking newsClickTracking);

    /**
     * 기존 뉴스의 클릭 횟수 증가
     */
    @Update("UPDATE news_click_tracking SET click_count = click_count + 1, " +
            "last_clicked_at = CURRENT_TIMESTAMP WHERE news_id = #{newsId}")
    int incrementClickCount(@Param("newsId") Integer newsId);

    /**
     * 뉴스 ID로 클릭 추적 정보 조회
     */
    @Select("SELECT nct.click_id as clickId, nct.news_id as newsId, nct.news_title as newsTitle, " +
            "n.source, nct.click_count as clickCount, nct.last_clicked_at as lastClickedAt, " +
            "nct.created_at as createdAt, nct.updated_at as updatedAt " +
            "FROM news_click_tracking nct " +
            "LEFT JOIN news n ON nct.news_id = n.news_id " +
            "WHERE nct.news_id = #{newsId}")
    NewsClickTracking findByNewsId(@Param("newsId") Integer newsId);

    /**
     * 인기 뉴스 조회 (클릭 횟수 기준 상위 N개) - 삭제된 뉴스 제외
     */
    @Select("SELECT nct.click_id as clickId, nct.news_id as newsId, nct.news_title as newsTitle, " +
            "n.source, nct.click_count as clickCount, nct.last_clicked_at as lastClickedAt, " +
            "nct.created_at as createdAt, nct.updated_at as updatedAt " +
            "FROM news_click_tracking nct " +
            "INNER JOIN news n ON nct.news_id = n.news_id " +
            "ORDER BY nct.click_count DESC, nct.last_clicked_at DESC LIMIT #{limit}")
    List<NewsClickTracking> findTopClickedNews(@Param("limit") int limit);

    /**
     * 최근 클릭된 뉴스 조회 - 삭제된 뉴스 제외
     */
    @Select("SELECT nct.click_id as clickId, nct.news_id as newsId, nct.news_title as newsTitle, " +
            "n.source, nct.click_count as clickCount, nct.last_clicked_at as lastClickedAt, " +
            "nct.created_at as createdAt, nct.updated_at as updatedAt " +
            "FROM news_click_tracking nct " +
            "INNER JOIN news n ON nct.news_id = n.news_id " +
            "ORDER BY nct.last_clicked_at DESC LIMIT #{limit}")
    List<NewsClickTracking> findRecentClickedNews(@Param("limit") int limit);

    /**
     * 전체 클릭 추적 목록 조회 (페이징) - 삭제된 뉴스 제외
     */
    @Select("SELECT nct.click_id as clickId, nct.news_id as newsId, nct.news_title as newsTitle, " +
            "n.source, nct.click_count as clickCount, nct.last_clicked_at as lastClickedAt, " +
            "nct.created_at as createdAt, nct.updated_at as updatedAt " +
            "FROM news_click_tracking nct " +
            "INNER JOIN news n ON nct.news_id = n.news_id " +
            "ORDER BY nct.click_count DESC LIMIT #{size} OFFSET #{offset}")
    List<NewsClickTracking> findAllNewsClickTracking(@Param("offset") int offset, @Param("size") int size);

    /**
     * 뉴스 제목 업데이트 (뉴스 정보 변경 시)
     */
    @Update("UPDATE news_click_tracking SET news_title = #{newsTitle} WHERE news_id = #{newsId}")
    int updateNewsTitle(@Param("newsId") Integer newsId, @Param("newsTitle") String newsTitle);

    /**
     * 오래된 클릭 기록 삭제 (90일 이상)
     */
    @Delete("DELETE FROM news_click_tracking WHERE last_clicked_at < DATE_SUB(NOW(), INTERVAL 90 DAY)")
    int deleteOldClickTracking();

    /**
     * 클릭 횟수가 적은 기록 삭제 (1회만 클릭된 것)
     */
    @Delete("DELETE FROM news_click_tracking WHERE click_count = 1 AND last_clicked_at < DATE_SUB(NOW(), INTERVAL 30 DAY)")
    int deleteLowClickTracking();

    /**
     * 총 클릭 추적 기록 개수
     */
    @Select("SELECT COUNT(*) FROM news_click_tracking")
    int getTotalCount();

    /**
     * 특정 뉴스의 총 클릭 수 조회
     */
    @Select("SELECT COALESCE(click_count, 0) FROM news_click_tracking WHERE news_id = #{newsId}")
    int getClickCountByNewsId(@Param("newsId") Integer newsId);
}


