package myapp.backend.domain.search.domain;

import java.time.LocalDateTime;

public class NewsClickTracking {
    private Integer clickId;
    private Integer newsId;
    private String newsTitle;
    private String source;  // 뉴스 출처 추가
    private Integer clickCount;
    private LocalDateTime lastClickedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자
    public NewsClickTracking() {}

    // 생성자 (새 클릭 추적용)
    public NewsClickTracking(Integer newsId, String newsTitle) {
        this.newsId = newsId;
        this.newsTitle = newsTitle;
        this.clickCount = 1;
    }

    // Getter & Setter
    public Integer getClickId() {
        return clickId;
    }

    public void setClickId(Integer clickId) {
        this.clickId = clickId;
    }

    public Integer getNewsId() {
        return newsId;
    }

    public void setNewsId(Integer newsId) {
        this.newsId = newsId;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getClickCount() {
        return clickCount;
    }

    public void setClickCount(Integer clickCount) {
        this.clickCount = clickCount;
    }

    public LocalDateTime getLastClickedAt() {
        return lastClickedAt;
    }

    public void setLastClickedAt(LocalDateTime lastClickedAt) {
        this.lastClickedAt = lastClickedAt;
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

    @Override
    public String toString() {
        return "NewsClickTracking{" +
                "clickId=" + clickId +
                ", newsId=" + newsId +
                ", newsTitle='" + newsTitle + '\'' +
                ", source='" + source + '\'' +
                ", clickCount=" + clickCount +
                ", lastClickedAt=" + lastClickedAt +
                '}';
    }
}


