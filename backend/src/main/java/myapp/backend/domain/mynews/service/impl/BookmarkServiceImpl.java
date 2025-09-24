package myapp.backend.domain.mynews.service.impl;

import myapp.backend.domain.mynews.domain.Bookmark;
import myapp.backend.domain.mynews.mapper.BookmarkMapper;
import myapp.backend.domain.mynews.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BookmarkServiceImpl implements BookmarkService {
    
    @Autowired
    private BookmarkMapper bookmarkMapper;
    
    @Override
    public Bookmark addBookmark(Integer userId, Integer newsId) {
        // 이미 북마크되어 있는지 확인
        Bookmark existingBookmark = bookmarkMapper.findByUserIdAndNewsId(userId, newsId);
        if (existingBookmark != null) {
            throw new RuntimeException("이미 북마크된 뉴스입니다.");
        }
        
        Bookmark bookmark = new Bookmark(userId, newsId);
        bookmarkMapper.insertBookmark(bookmark);
        
        // 삽입된 북마크 정보를 다시 조회하여 반환
        return bookmarkMapper.findByUserIdAndNewsId(userId, newsId);
    }
    
    @Override
    public boolean deleteBookmark(Integer bookmarkId, Integer userId) {
        int result = bookmarkMapper.deleteBookmark(bookmarkId, userId);
        return result > 0;
    }
    
    @Override
    public boolean deleteBookmarkByNewsId(Integer userId, Integer newsId) {
        int result = bookmarkMapper.deleteBookmarkByNewsId(userId, newsId);
        return result > 0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Bookmark> getBookmarksByUserId(Integer userId) {
        return bookmarkMapper.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Bookmark getBookmarkByUserIdAndNewsId(Integer userId, Integer newsId) {
        return bookmarkMapper.findByUserIdAndNewsId(userId, newsId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getBookmarkCountByUserId(Integer userId) {
        return bookmarkMapper.countByUserId(userId);
    }
}






