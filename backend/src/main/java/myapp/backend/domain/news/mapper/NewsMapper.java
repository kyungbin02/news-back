package myapp.backend.domain.news.mapper;

import myapp.backend.domain.news.domain.News;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NewsMapper {

    /**
     * 뉴스 삽입
     */
    @Insert("INSERT INTO news (title, content, category, image_url, views) " +
            "VALUES (#{title}, #{content}, #{category}, #{imageUrl}, #{views})")
    @Options(useGeneratedKeys = true, keyProperty = "newsId")
    int insertNews(News news);

    // <경빈> RSS 뉴스 저장 (외부에서 지정된 newsId 사용)
    @Insert("INSERT INTO news (news_id, title, content, category, image_url, views, source, url, published_at, created_at) " +
            "VALUES (#{newsId}, #{title}, #{content}, #{category}, #{imageUrl}, #{views}, #{source}, #{url}, #{publishedAt}, #{createdAt})")
    int insertRssNews(News news);

    /**
     * 뉴스 업데이트
     */
    @Update("UPDATE news SET title = #{title}, content = #{content}, " +
            "category = #{category}, image_url = #{imageUrl}, views = #{views}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE news_id = #{newsId}")
    int updateNews(News news);

    /**
     * 제목으로 뉴스 찾기 (중복 체크용)
     */
    @Select("SELECT news_id as newsId, title, content, category, image_url as imageUrl, views, created_at as createdAt, updated_at as updatedAt, source, url, published_at as publishedAt FROM news WHERE title = #{title}")
    News findByTitle(@Param("title") String title);

    // <경빈> newsId로 뉴스 존재 여부 확인 (중복 체크용)
    @Select("SELECT COUNT(*) FROM news WHERE news_id = #{newsId}")
    int existsByNewsId(@Param("newsId") Integer newsId);

    /**
     * ID로 뉴스 찾기
     */
    @Select("SELECT news_id as newsId, title, content, category, image_url as imageUrl, views, created_at as createdAt, updated_at as updatedAt, source, url, published_at as publishedAt FROM news WHERE news_id = #{newsId}")
    News findById(@Param("newsId") long newsId);

    /**
     * 전체 뉴스 목록 조회 (페이징)
     */
    @Select("SELECT news_id as newsId, title, content, category, image_url as imageUrl, views, created_at as createdAt, updated_at as updatedAt, source, url, published_at as publishedAt FROM news ORDER BY created_at DESC LIMIT #{size} OFFSET #{offset}")
    List<News> findAllNews(@Param("offset") int offset, @Param("size") int size);

    /**
     * 카테고리별 뉴스 목록 조회 (페이징)
     */
    @Select("SELECT news_id as newsId, title, content, category, image_url as imageUrl, views, created_at as createdAt, updated_at as updatedAt, source, url, published_at as publishedAt FROM news WHERE category = #{category} ORDER BY created_at DESC LIMIT #{size} OFFSET #{offset}")
    List<News> findNewsByCategory(@Param("category") String category, @Param("offset") int offset, @Param("size") int size);

    /**
     * 최신 뉴스 조회
     */
    @Select("SELECT news_id as newsId, title, content, category, image_url as imageUrl, views, created_at as createdAt, updated_at as updatedAt, source, url, published_at as publishedAt FROM news ORDER BY created_at DESC LIMIT #{limit}")
    List<News> findLatestNews(@Param("limit") int limit);

    /**
     * 인기 뉴스 조회 (조회수 기준)
     */
    @Select("SELECT news_id as newsId, title, content, category, image_url as imageUrl, views, created_at as createdAt, updated_at as updatedAt, source, url, published_at as publishedAt FROM news ORDER BY views DESC, created_at DESC LIMIT #{limit}")
    List<News> findPopularNews(@Param("limit") int limit);

    /**
     * 조회수 증가
     */
    @Update("UPDATE news SET views = views + 1, updated_at = CURRENT_TIMESTAMP WHERE news_id = #{newsId}")
    int incrementViews(@Param("newsId") int newsId);

    /**
     * 검색 (제목, 내용 기준)
     */
    @Select("SELECT news_id as newsId, title, content, category, image_url as imageUrl, views, created_at as createdAt, updated_at as updatedAt, source, url, published_at as publishedAt FROM news WHERE title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY created_at DESC LIMIT #{size} OFFSET #{offset}")
    List<News> searchNews(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    /**
     * 총 뉴스 개수
     */
    @Select("SELECT COUNT(*) FROM news")
    int getTotalCount();

    /**
     * URL이 null인 뉴스 조회
     */
    @Select("SELECT news_id as newsId, title, content, category, image_url as imageUrl, views, created_at as createdAt, updated_at as updatedAt, source, url, published_at as publishedAt FROM news WHERE url IS NULL OR url = ''")
    List<News> findNewsWithNullUrl();

    /**
     * URL이 null인 경우에만 업데이트
     */
    @Update("UPDATE news SET url = #{url}, updated_at = CURRENT_TIMESTAMP WHERE news_id = #{newsId} AND (url IS NULL OR url = '')")
    int updateUrlIfNull(@Param("newsId") int newsId, @Param("url") String url);

    /**
     * 카테고리별 뉴스 개수
     */
    @Select("SELECT COUNT(*) FROM news WHERE category = #{category}")
    int getCountByCategory(@Param("category") String category);

    /**
     * 오래된 뉴스 삭제 (10일 이상)
     */
    @Delete("DELETE FROM news WHERE created_at < DATE_SUB(NOW(), INTERVAL 10 DAY)")
    int deleteOldNews();

    /**
     * 기존 뉴스의 source 필드 업데이트 (null인 경우에만)
     */
    @Update("UPDATE news SET source = #{source} WHERE news_id = #{newsId} AND source IS NULL")
    int updateSourceIfNull(@Param("newsId") Integer newsId, @Param("source") String source);

    /**
     * source가 null인 뉴스 목록 조회
     */
    @Select("SELECT news_id as newsId, title, content, category, image_url as imageUrl, views, created_at as createdAt, updated_at as updatedAt, source, url, published_at as publishedAt FROM news WHERE source IS NULL")
    List<News> findNewsWithNullSource();

    /**
     * 뉴스 카테고리 업데이트
     */
    @Update("UPDATE news SET category = #{category}, updated_at = CURRENT_TIMESTAMP WHERE news_id = #{newsId}")
    int updateNewsCategory(@Param("newsId") Integer newsId, @Param("category") String category);
}