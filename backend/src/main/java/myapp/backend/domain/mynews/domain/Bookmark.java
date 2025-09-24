package myapp.backend.domain.mynews.domain;

import java.time.LocalDateTime;

public class Bookmark {
    private Integer bookmarkId;
    private Integer userId;
    private Integer newsId;
    private LocalDateTime createdAt;
    
    // 뉴스 정보 (JOIN용)
    private String newsTitle;
    private String newsContent;
    private String imageUrl;
    private String category;
    private LocalDateTime newsCreatedAt;
    
    public Bookmark() {}
    
    public Bookmark(Integer userId, Integer newsId) {
        this.userId = userId;
        this.newsId = newsId;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getBookmarkId() {
        return bookmarkId;
    }
    
    public void setBookmarkId(Integer bookmarkId) {
        this.bookmarkId = bookmarkId;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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






