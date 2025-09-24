package myapp.backend.domain.mynews.service;

import myapp.backend.domain.mynews.domain.Bookmark;
import java.util.List;

public interface BookmarkService {
    
    /**
     * 북마크 추가
     */
    Bookmark addBookmark(Integer userId, Integer newsId);
    
    /**
     * 북마크 삭제 (북마크 ID로)
     */
    boolean deleteBookmark(Integer bookmarkId, Integer userId);
    
    /**
     * 북마크 삭제 (뉴스 ID로)
     */
    boolean deleteBookmarkByNewsId(Integer userId, Integer newsId);
    
    /**
     * 사용자의 북마크 목록 조회
     */
    List<Bookmark> getBookmarksByUserId(Integer userId);
    
    /**
     * 특정 뉴스의 북마크 여부 확인
     */
    Bookmark getBookmarkByUserIdAndNewsId(Integer userId, Integer newsId);
    
    /**
     * 사용자의 북마크 개수 조회
     */
    int getBookmarkCountByUserId(Integer userId);
}






