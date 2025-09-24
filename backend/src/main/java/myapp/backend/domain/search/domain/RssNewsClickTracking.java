package myapp.backend.domain.search.domain;

import java.time.LocalDateTime;

public class RssNewsClickTracking {
    private Integer clickId;
    private String rssNewsId;
    private String newsTitle;
    private Integer clickCount;
    private LocalDateTime lastClickedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자
    public RssNewsClickTracking() {}

    // 생성자 (새 클릭 추적용)
    public RssNewsClickTracking(String rssNewsId, String newsTitle) {
        this.rssNewsId = rssNewsId;
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

    public String getRssNewsId() {
        return rssNewsId;
    }

    public void setRssNewsId(String rssNewsId) {
        this.rssNewsId = rssNewsId;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
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
        return "RssNewsClickTracking{" +
                "clickId=" + clickId +
                ", rssNewsId='" + rssNewsId + '\'' +
                ", newsTitle='" + newsTitle + '\'' +
                ", clickCount=" + clickCount +
                ", lastClickedAt=" + lastClickedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}







