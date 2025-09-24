package myapp.backend.domain.search.mapper;

import myapp.backend.domain.search.domain.RssNewsClickTracking;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface RssNewsClickTrackingMapper {

    /**
     * 새로운 RSS 뉴스 클릭 추적 삽입
     */
    @Insert("INSERT INTO rss_news_click_tracking (rss_news_id, news_title, click_count, last_clicked_at) " +
            "VALUES (#{rssNewsId}, #{newsTitle}, #{clickCount}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "clickId")
    int insertRssNewsClickTracking(RssNewsClickTracking rssNewsClickTracking);

    /**
     * 기존 RSS 뉴스의 클릭 횟수 증가
     */
    @Update("UPDATE rss_news_click_tracking SET click_count = click_count + 1, " +
            "last_clicked_at = CURRENT_TIMESTAMP WHERE rss_news_id = #{rssNewsId}")
    int incrementClickCount(@Param("rssNewsId") String rssNewsId);

    /**
     * RSS 뉴스 ID로 클릭 추적 정보 조회
     */
    @Select("SELECT click_id as clickId, rss_news_id as rssNewsId, news_title as newsTitle, " +
            "click_count as clickCount, last_clicked_at as lastClickedAt, " +
            "created_at as createdAt, updated_at as updatedAt " +
            "FROM rss_news_click_tracking WHERE rss_news_id = #{rssNewsId}")
    RssNewsClickTracking findByRssNewsId(@Param("rssNewsId") String rssNewsId);

    /**
     * 인기 RSS 뉴스 조회 (클릭 횟수 기준 상위 N개)
     */
    @Select("SELECT click_id as clickId, rss_news_id as rssNewsId, news_title as newsTitle, " +
            "click_count as clickCount, last_clicked_at as lastClickedAt, " +
            "created_at as createdAt, updated_at as updatedAt " +
            "FROM rss_news_click_tracking ORDER BY click_count DESC, last_clicked_at DESC LIMIT #{limit}")
    List<RssNewsClickTracking> findTopClickedRssNews(@Param("limit") int limit);

    /**
     * 최근 클릭된 RSS 뉴스 조회
     */
    @Select("SELECT click_id as clickId, rss_news_id as rssNewsId, news_title as newsTitle, " +
            "click_count as clickCount, last_clicked_at as lastClickedAt, " +
            "created_at as createdAt, updated_at as updatedAt " +
            "FROM rss_news_click_tracking ORDER BY last_clicked_at DESC LIMIT #{limit}")
    List<RssNewsClickTracking> findRecentClickedRssNews(@Param("limit") int limit);

    /**
     * 전체 RSS 뉴스 클릭 추적 개수 조회
     */
    @Select("SELECT COUNT(*) FROM rss_news_click_tracking")
    int getTotalCount();
}







