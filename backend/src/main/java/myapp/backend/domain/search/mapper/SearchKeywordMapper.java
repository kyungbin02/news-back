package myapp.backend.domain.search.mapper;

import myapp.backend.domain.search.domain.SearchKeyword;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SearchKeywordMapper {

    /**
     * 검색어 삽입 (새로운 검색어)
     */
    @Insert("INSERT INTO search_keywords (keyword, search_count, last_searched_at) " +
            "VALUES (#{keyword}, #{searchCount}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "searchId")
    int insertSearchKeyword(SearchKeyword searchKeyword);

    /**
     * 기존 검색어의 검색 횟수 증가
     */
    @Update("UPDATE search_keywords SET search_count = search_count + 1, " +
            "last_searched_at = CURRENT_TIMESTAMP WHERE keyword = #{keyword}")
    int incrementSearchCount(@Param("keyword") String keyword);

    /**
     * 검색어로 조회 (중복 체크용)
     */
    @Select("SELECT search_id as searchId, keyword, search_count as searchCount, " +
            "last_searched_at as lastSearchedAt, created_at as createdAt, updated_at as updatedAt " +
            "FROM search_keywords WHERE keyword = #{keyword}")
    SearchKeyword findByKeyword(@Param("keyword") String keyword);

    /**
     * 인기 검색어 조회 (검색 횟수 기준 상위 N개)
     */
    @Select("SELECT search_id as searchId, keyword, search_count as searchCount, " +
            "last_searched_at as lastSearchedAt, created_at as createdAt, updated_at as updatedAt " +
            "FROM search_keywords ORDER BY search_count DESC, last_searched_at DESC LIMIT #{limit}")
    List<SearchKeyword> findTopSearchKeywords(@Param("limit") int limit);

    /**
     * 최근 검색어 조회 (최근 검색 시간 기준)
     */
    @Select("SELECT search_id as searchId, keyword, search_count as searchCount, " +
            "last_searched_at as lastSearchedAt, created_at as createdAt, updated_at as updatedAt " +
            "FROM search_keywords ORDER BY last_searched_at DESC LIMIT #{limit}")
    List<SearchKeyword> findRecentSearchKeywords(@Param("limit") int limit);

    /**
     * 전체 검색어 목록 조회 (페이징)
     */
    @Select("SELECT search_id as searchId, keyword, search_count as searchCount, " +
            "last_searched_at as lastSearchedAt, created_at as createdAt, updated_at as updatedAt " +
            "FROM search_keywords ORDER BY search_count DESC LIMIT #{size} OFFSET #{offset}")
    List<SearchKeyword> findAllSearchKeywords(@Param("offset") int offset, @Param("size") int size);

    /**
     * 오래된 검색어 삭제 (30일 이상)
     */
    @Delete("DELETE FROM search_keywords WHERE last_searched_at < DATE_SUB(NOW(), INTERVAL 30 DAY)")
    int deleteOldSearchKeywords();

    /**
     * 검색 횟수가 적은 검색어 삭제 (1회만 검색된 것)
     */
    @Delete("DELETE FROM search_keywords WHERE search_count = 1 AND last_searched_at < DATE_SUB(NOW(), INTERVAL 7 DAY)")
    int deleteLowCountSearchKeywords();

    /**
     * 총 검색어 개수
     */
    @Select("SELECT COUNT(*) FROM search_keywords")
    int getTotalCount();
}


