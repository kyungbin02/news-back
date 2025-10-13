package myapp.backend.domain.news.domain;

import java.time.LocalDateTime;

public class News {
    private Integer newsId;
    private String title;
    private String content;
    private String category;
    private String imageUrl;
    private Integer views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // <경빈> RSS 뉴스 통합을 위한 필드 추가
    private String source;        // RSS 출처 (예: chosun.com)
    private String url;          // 원본 링크
    private LocalDateTime publishedAt;  // RSS 발행일
    private String summary;      // AI 요약 내용 (JSON 형태)

    // 기본 생성자
    public News() {}

    // 전체 생성자
    public News(String title, String content, String category, String imageUrl) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.imageUrl = imageUrl;
        this.views = 0;
    }

    // Getter & Setter
    public Integer getNewsId() {
        return newsId;
    }

    public void setNewsId(Integer newsId) {
        this.newsId = newsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // <경빈> RSS 관련 필드 getter/setter
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "News{" +
                "newsId=" + newsId +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", views=" + views +
                ", createdAt=" + createdAt +
                ", source='" + source + '\'' +
                ", url='" + url + '\'' +
                ", publishedAt=" + publishedAt +
                '}';
    }
}