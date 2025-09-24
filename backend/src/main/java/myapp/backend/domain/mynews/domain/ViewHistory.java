package myapp.backend.domain.mynews.domain;

import java.time.LocalDateTime;

public class ViewHistory {
    private Integer viewId;
    private Integer userId;
    private Integer newsId;
    private LocalDateTime viewedAt;
    private Integer readTime;
    
    // 뉴스 정보 (JOIN용)
    private String newsTitle;
    private String newsContent;
    private String imageUrl;
    private String category;
    private LocalDateTime newsCreatedAt;
    
    public ViewHistory() {}
    
    public ViewHistory(Integer userId, Integer newsId) {
        this.userId = userId;
        this.newsId = newsId;
        this.viewedAt = LocalDateTime.now();
        this.readTime = 0;
    }
    
    public ViewHistory(Integer userId, Integer newsId, Integer readTime) {
        this.userId = userId;
        this.newsId = newsId;
        this.viewedAt = LocalDateTime.now();
        this.readTime = readTime;
    }
    
    // Getters and Setters
    public Integer getViewId() {
        return viewId;
    }
    
    public void setViewId(Integer viewId) {
        this.viewId = viewId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getNewsId() {
        return newsId;
    }
    
    public void setNewsId(Integer newsId) {
        this.newsId = newsId;
    }
    
    public LocalDateTime getViewedAt() {
        return viewedAt;
    }
    
    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
    
    public Integer getReadTime() {
        return readTime;
    }
    
    public void setReadTime(Integer readTime) {
        this.readTime = readTime;
    }
    
    public String getNewsTitle() {
        return newsTitle;
    }
    
    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }
    
    public String getNewsContent() {
        return newsContent;
    }
    
    public void setNewsContent(String newsContent) {
        this.newsContent = newsContent;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public LocalDateTime getNewsCreatedAt() {
        return newsCreatedAt;
    }
    
    public void setNewsCreatedAt(LocalDateTime newsCreatedAt) {
        this.newsCreatedAt = newsCreatedAt;
    }
}






