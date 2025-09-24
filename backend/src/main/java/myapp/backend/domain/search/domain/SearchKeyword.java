package myapp.backend.domain.search.domain;

import java.time.LocalDateTime;

public class SearchKeyword {
    private Integer searchId;
    private String keyword;
    private Integer searchCount;
    private LocalDateTime lastSearchedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자
    public SearchKeyword() {}

    // 생성자 (새 검색어용)
    public SearchKeyword(String keyword) {
        this.keyword = keyword;
        this.searchCount = 1;
    }

    // Getter & Setter
    public Integer getSearchId() {
        return searchId;
    }

    public void setSearchId(Integer searchId) {
        this.searchId = searchId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(Integer searchCount) {
        this.searchCount = searchCount;
    }

    public LocalDateTime getLastSearchedAt() {
        return lastSearchedAt;
    }

    public void setLastSearchedAt(LocalDateTime lastSearchedAt) {
        this.lastSearchedAt = lastSearchedAt;
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
        return "SearchKeyword{" +
                "searchId=" + searchId +
                ", keyword='" + keyword + '\'' +
                ", searchCount=" + searchCount +
                ", lastSearchedAt=" + lastSearchedAt +
                '}';
    }
}


