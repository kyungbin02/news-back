package myapp.backend.domain.mynews.service.impl;

import myapp.backend.domain.mynews.domain.ViewHistory;
import myapp.backend.domain.mynews.mapper.ViewHistoryMapper;
import myapp.backend.domain.mynews.service.ViewHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ViewHistoryServiceImpl implements ViewHistoryService {
    
    @Autowired
    private ViewHistoryMapper viewHistoryMapper;
    
    @Override
    public ViewHistory addViewHistory(Integer userId, Integer newsId) {
        ViewHistory viewHistory = new ViewHistory(userId, newsId);
        viewHistoryMapper.insertViewHistory(viewHistory);
        
        // 삽입된 조회 기록을 다시 조회하여 반환
        return viewHistoryMapper.findLatestByUserIdAndNewsId(userId, newsId);
    }
    
    @Override
    public ViewHistory addViewHistory(Integer userId, Integer newsId, Integer readTime) {
        ViewHistory viewHistory = new ViewHistory(userId, newsId, readTime);
        viewHistoryMapper.insertViewHistory(viewHistory);
        
        // 삽입된 조회 기록을 다시 조회하여 반환
        return viewHistoryMapper.findLatestByUserIdAndNewsId(userId, newsId);
    }
    
    @Override
    public boolean updateReadTime(Integer viewId, Integer userId, Integer readTime) {
        int result = viewHistoryMapper.updateReadTime(viewId, userId, readTime);
        return result > 0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ViewHistory> getViewHistoryByUserId(Integer userId, int limit) {
        return viewHistoryMapper.findByUserIdWithLimit(userId, limit);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ViewHistory> getViewHistoryByUserId(Integer userId) {
        return viewHistoryMapper.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ViewHistory getLatestViewHistoryByUserIdAndNewsId(Integer userId, Integer newsId) {
        return viewHistoryMapper.findLatestByUserIdAndNewsId(userId, newsId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getViewHistoryCountByUserId(Integer userId) {
        return viewHistoryMapper.countByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getTotalReadTimeByUserId(Integer userId) {
        return viewHistoryMapper.getTotalReadTimeByUserId(userId);
    }
}






