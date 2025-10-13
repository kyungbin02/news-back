package myapp.backend.domain.news.service.impl;

import myapp.backend.domain.news.domain.News;
import myapp.backend.domain.news.mapper.NewsMapper;
import myapp.backend.domain.news.service.RssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RssServiceImpl implements RssService {

    @Autowired
    private NewsMapper newsMapper;

    private final RestTemplate restTemplate;
    
    public RssServiceImpl() {
        this.restTemplate = new RestTemplate();
        // UTF-8 인코딩을 위한 MessageConverter 설정
        this.restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    // RSS 피드 URL들 (최고 안정성 2개 언론사만 유지)
    private final String[] RSS_URLS = {
        // === 동아일보 (3개 피드) ===
        "https://rss.donga.com/total.xml",                  // 동아일보 전체 (확인됨)
        "https://rss.donga.com/economy.xml",                // 동아일보 경제 (확인됨)
        "https://rss.donga.com/sports.xml",                 // 동아일보 스포츠 (확인됨)
        
        // === 중앙일보 (1개 피드) ===
        "https://rss.joins.com/joins_news_list.xml"        // 중앙일보 전체 (확인됨)
    };

    @Override
    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void collectRssNews() {
        System.out.println("RSS 뉴스 수집 시작: " + LocalDateTime.now());
        
        int successCount = 0;
        int failCount = 0;
        
        for (String rssUrl : RSS_URLS) {
            try {
                collectFromSingleRss(rssUrl);
                successCount++;
                System.out.println("✓ RSS 수집 성공: " + rssUrl);
            } catch (Exception e) {
                failCount++;
                System.err.println("✗ RSS 수집 실패: " + rssUrl + " - " + e.getMessage());
                
                // 피드 장애 시 대체 피드 시도 (동일 카테고리 내에서)
                tryAlternativeFeed(rssUrl, e);
            }
        }
        
        System.out.println("RSS 뉴스 수집 완료: " + LocalDateTime.now());
        System.out.println("성공: " + successCount + "개, 실패: " + failCount + "개");
        
        // 피드 안정성 리포트
        if (failCount > RSS_URLS.length * 0.3) { // 30% 이상 실패 시 경고
            System.err.println("⚠️ RSS 피드 안정성 경고: " + failCount + "개 피드에서 오류 발생");
        }
        
        // 뉴스 개수 체크 및 정리 (서버 상태와 관계없이 작동)
        // checkAndCleanupNews(); // 주석 처리됨 - 나중에 "뉴스 삭제 기능 다시 활성화해줘"라고 요청하면 복구 가능
    }

    /**
     * 뉴스 개수 체크 및 정리
     * 뉴스가 너무 많아지면 자동으로 오래된 뉴스 삭제
     * 
     * 주석 처리됨 - 나중에 "뉴스 삭제 기능 다시 활성화해줘"라고 요청하면 복구 가능
     */
    /*
    private void checkAndCleanupNews() {
        try {
            int totalNews = newsMapper.getTotalCount();
            System.out.println("현재 총 뉴스 개수: " + totalNews);
            
            // 1500개 이상이면 정리 실행
            if (totalNews > 1500) {
                System.out.println("⚠️ 뉴스 개수가 많아서 정리 실행: " + totalNews);
                int deletedCount = newsMapper.deleteOldNews();
                System.out.println("✅ 뉴스 정리 완료: " + deletedCount + "개 삭제");
                
                // 정리 후 개수 확인
                int afterCleanup = newsMapper.getTotalCount();
                System.out.println("정리 후 총 뉴스 개수: " + afterCleanup);
            } else {
                System.out.println("뉴스 개수 정상: " + totalNews + "개");
            }
        } catch (Exception e) {
            System.err.println("뉴스 정리 중 오류 발생: " + e.getMessage());
        }
    }
    */

    /**
     * 피드 장애 시 동일 카테고리의 대체 피드 시도
     */
    private void tryAlternativeFeed(String failedUrl, Exception originalError) {
        String category = determineCategoryFromUrl(failedUrl);
        System.out.println("대체 피드 시도: " + failedUrl + " (카테고리: " + category + ")");
        
        // 동일 카테고리의 다른 피드 찾기
        for (String alternativeUrl : RSS_URLS) {
            if (!alternativeUrl.equals(failedUrl) && 
                determineCategoryFromUrl(alternativeUrl).equals(category)) {
                try {
                    collectFromSingleRss(alternativeUrl);
                    System.out.println("✓ 대체 피드 성공: " + alternativeUrl);
                    return;
                } catch (Exception e) {
                    System.err.println("✗ 대체 피드도 실패: " + alternativeUrl);
                }
            }
        }
        
        System.err.println("⚠️ 카테고리 '" + category + "'의 모든 피드가 장애 상태입니다.");
    }

    private void collectFromSingleRss(String rssUrl) throws Exception {
        // HTTP 헤더에 UTF-8 인코딩 명시
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept-Charset", "UTF-8");
        headers.set("Content-Type", "application/xml; charset=UTF-8");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(rssUrl, HttpMethod.GET, entity, String.class);
        String xmlContent = response.getBody();
        
        if (xmlContent == null) {
            throw new Exception("RSS 피드 내용을 가져올 수 없습니다: " + rssUrl);
        }
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
        
        NodeList items = document.getElementsByTagName("item");
        
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            
            try {
                News news = parseNewsFromItem(item, rssUrl);
                
                // 중복 체크 (제목으로만 판단)
                News existingNews = newsMapper.findByTitle(news.getTitle());
                
                if (existingNews == null) {
                    newsMapper.insertRssNews(news);
                    System.out.println("새 뉴스 저장: " + news.getTitle() + " (출처: " + news.getSource() + ")");
                }
                // 기존 뉴스가 있으면 스킵 (또는 content만 업데이트)
                
            } catch (Exception e) {
                System.err.println("뉴스 파싱/저장 실패: " + e.getMessage());
            }
        }
    }

    private News parseNewsFromItem(Element item, String rssUrl) {
        News news = new News();
        
        // 기본 정보 추출
        String title = getElementText(item, "title");
        String description = getElementText(item, "description");
        String url = getElementText(item, "link");  // 원본 링크 추출
        
        // HTML 태그 제거
        title = cleanHtmlTags(title);
        description = cleanHtmlTags(description);
        
        // 동아일보 URL 형식 검증 및 수정
        url = validateAndFixDongaUrl(url, rssUrl);
        
        // 카테고리 결정
        String category = determineCategoryFromUrl(rssUrl);
        System.out.println("RSS URL: " + rssUrl + " → 카테고리: " + category);
        
        // 이미지 URL 추출 (description과 item 전체에서 시도)
        String imageUrl = extractImageUrl(description);
        if (imageUrl == null) {
            // description에서 못찾으면 item 전체에서 시도
            imageUrl = extractImageFromItem(item);
        }
        
        // RSS URL에서 출처명 추출
        String source = determineSourceFromUrl(rssUrl);
        
        news.setTitle(title);
        news.setContent(description);  // content에 description 저장
        news.setCategory(category);
        news.setImageUrl(imageUrl);
        news.setViews(0);
        news.setSource(source);  // RSS 출처 설정
        news.setUrl(url);  // 원본 링크 설정
        news.setCreatedAt(LocalDateTime.now());  // 생성 시간 설정
        news.setPublishedAt(LocalDateTime.now());  // 발행 시간 설정
        
        return news;
    }

    private String getElementText(Element parent, String tagName) {
        try {
            NodeList nodes = parent.getElementsByTagName(tagName);
            if (nodes.getLength() > 0) {
                return nodes.item(0).getTextContent().trim();
            }
        } catch (Exception e) {
            // 무시
        }
        return "";
    }

    private String cleanHtmlTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]+>", "").trim();
    }

    private String determineCategoryFromUrl(String rssUrl) {
        // 스포츠 카테고리
        if (rssUrl.contains("sports")) return "sports";
        
        // 경제 카테고리
        if (rssUrl.contains("economy")) return "economy";
        
        // 전체/종합 뉴스는 general로 분류
        if (rssUrl.contains("total") || rssUrl.contains("joins.com")) return "general";
        
        return "general"; // 기본값
    }

    /**
     * RSS URL에서 언론사명 추출
     */
    private String determineSourceFromUrl(String rssUrl) {
        if (rssUrl.contains("donga.com")) return "동아일보";
        if (rssUrl.contains("joins.com")) return "중앙일보";
        return "외부뉴스";
    }

    /**
     * 동아일보 URL 형식 검증 및 수정
     * 잘못된 형식: /news/article/all/뉴스ID
     * 올바른 형식: /news/Inter/article/all/YYYYMMDD/기사ID/페이지
     */
    private String validateAndFixDongaUrl(String url, String rssUrl) {
        if (url == null || !url.contains("donga.com")) {
            return url; // 동아일보가 아니면 그대로 반환
        }
        
        // 잘못된 형식 패턴: /news/article/all/숫자
        Pattern wrongPattern = Pattern.compile("(https://www\\.donga\\.com)/news/article/all/(\\d+)");
        Matcher wrongMatcher = wrongPattern.matcher(url);
        
        if (wrongMatcher.find()) {
            String baseUrl = wrongMatcher.group(1);
            String newsId = wrongMatcher.group(2);
            
            // RSS URL에서 카테고리 추출
            String category = "";
            if (rssUrl.contains("economy")) {
                category = "Economy";
            } else if (rssUrl.contains("sports")) {
                category = "Sports";
            } else {
                category = "Inter"; // 전체 뉴스는 Inter로 분류
            }
            
            // 현재 날짜로 올바른 형식 생성
            LocalDateTime now = LocalDateTime.now();
            String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 올바른 형식으로 URL 재구성
            String correctUrl = baseUrl + "/news/" + category + "/article/all/" + dateStr + "/" + newsId + "/1";
            
            System.out.println("URL 형식 수정: " + url + " → " + correctUrl);
            return correctUrl;
        }
        
        return url; // 이미 올바른 형식이면 그대로 반환
    }

    private String extractImageUrl(String description) {
        if (description == null) return null;
        
        // 디버깅을 위한 로그 추가
        System.out.println("=== 이미지 추출 시도 ===");
        System.out.println("Description 길이: " + description.length());
        System.out.println("Description 일부: " + description.substring(0, Math.min(200, description.length())));
        
        // 다양한 이미지 패턴 시도
        String[] patterns = {
            "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>", // 기본 img 태그
            "<image[^>]+url\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>", // RSS image 태그
            "<media:content[^>]+url\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>", // media namespace
            "<enclosure[^>]+url\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>", // enclosure 태그
            "https?://[^\\s]+\\.(jpg|jpeg|png|gif|webp)", // 직접 이미지 URL
        };
        
        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(description);
            
            if (matcher.find()) {
                String imageUrl = matcher.group(1);
                System.out.println("이미지 URL 발견: " + imageUrl + " (패턴: " + patternStr + ")");
                return imageUrl;
            }
        }
        
        System.out.println("이미지 URL을 찾지 못함");
        return null;
    }

    private String extractImageFromItem(Element item) {
        try {
            // enclosure 태그에서 이미지 찾기
            NodeList enclosures = item.getElementsByTagName("enclosure");
            for (int i = 0; i < enclosures.getLength(); i++) {
                Element enclosure = (Element) enclosures.item(i);
                String url = enclosure.getAttribute("url");
                String type = enclosure.getAttribute("type");
                if (url != null && type != null && type.startsWith("image/")) {
                    System.out.println("Enclosure에서 이미지 발견: " + url);
                    return url;
                }
            }

            // media:content 태그에서 이미지 찾기
            NodeList mediaContents = item.getElementsByTagName("media:content");
            for (int i = 0; i < mediaContents.getLength(); i++) {
                Element mediaContent = (Element) mediaContents.item(i);
                String url = mediaContent.getAttribute("url");
                String type = mediaContent.getAttribute("type");
                if (url != null && (type == null || type.startsWith("image/"))) {
                    System.out.println("media:content에서 이미지 발견: " + url);
                    return url;
                }
            }

            // image 태그에서 이미지 찾기
            NodeList images = item.getElementsByTagName("image");
            for (int i = 0; i < images.getLength(); i++) {
                Element image = (Element) images.item(i);
                String url = getElementText(image, "url");
                if (url != null && !url.trim().isEmpty()) {
                    System.out.println("image 태그에서 이미지 발견: " + url);
                    return url;
                }
            }

            System.out.println("RSS item에서 이미지를 찾지 못함");
            return null;
        } catch (Exception e) {
            System.err.println("RSS item 이미지 추출 실패: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<News> getAllNews(int page, int size) {
        int offset = (page - 1) * size;
        return newsMapper.findAllNews(offset, size);
    }

    @Override
    public List<News> getNewsByCategory(String category, int page, int size) {
        int offset = (page - 1) * size;
        return newsMapper.findNewsByCategory(category, offset, size);
    }

    @Override
    public News getNewsById(long newsId) {
        return newsMapper.findById(newsId);
    }

    @Override
    public void incrementViews(int newsId) {
        newsMapper.incrementViews(newsId);
    }

    @Override
    public List<News> searchNews(String keyword, int page, int size) {
        int offset = (page - 1) * size;
        return newsMapper.searchNews(keyword, offset, size);
    }

    @Override
    public List<News> getPopularNews(int limit) {
        return newsMapper.findPopularNews(limit);
    }

    public News findByTitle(String title) {
        return newsMapper.findByTitle(title);
    }

    /**
     * 기존 뉴스의 source 필드 업데이트 (null인 경우에만)
     */
    @Override
    public void updateNullSourceNews() {
        System.out.println("=== 기존 뉴스 source 필드 업데이트 시작 ===");
        
        List<News> nullSourceNews = newsMapper.findNewsWithNullSource();
        System.out.println("source가 null인 뉴스 개수: " + nullSourceNews.size());
        
        for (News news : nullSourceNews) {
            // 카테고리별로 기본 출처 설정
            String defaultSource = getDefaultSourceByCategory(news.getCategory());
            
            int updateResult = newsMapper.updateSourceIfNull(news.getNewsId(), defaultSource);
            if (updateResult > 0) {
                System.out.println("뉴스 ID " + news.getNewsId() + " source 업데이트: " + defaultSource);
            }
        }
        
        System.out.println("=== 기존 뉴스 source 필드 업데이트 완료 ===");
    }

    /**
     * 기존 뉴스들의 URL 필드 업데이트 (null인 경우에만)
     */
    @Override
    public void updateNullUrlNews() {
        System.out.println("=== 기존 뉴스 URL 필드 업데이트 시작 ===");
        
        List<News> nullUrlNews = newsMapper.findNewsWithNullUrl();
        System.out.println("URL이 null인 뉴스 개수: " + nullUrlNews.size());
        
        for (News news : nullUrlNews) {
            // 기본 URL 설정 (동아일보 기사 링크 형식)
            String defaultUrl = "https://www.donga.com/news/article/all/" + news.getNewsId();
            
            int updateResult = newsMapper.updateUrlIfNull(news.getNewsId(), defaultUrl);
            if (updateResult > 0) {
                System.out.println("뉴스 ID " + news.getNewsId() + " URL 업데이트: " + defaultUrl);
            }
        }
        
        System.out.println("=== 기존 뉴스 URL 필드 업데이트 완료 ===");
    }

    /**
     * 잘못된 형식의 동아일보 URL을 올바른 형식으로 수정
     */
    @Override
    public void fixDongaUrlFormat() {
        System.out.println("=== 동아일보 URL 형식 수정 시작 ===");
        
        // 모든 뉴스 조회 (최대 1000개)
        List<News> allNews = newsMapper.findAllNews(0, 1000);
        System.out.println("총 뉴스 개수: " + allNews.size());
        
        int fixedCount = 0;
        for (News news : allNews) {
            if (news.getUrl() != null && news.getUrl().contains("donga.com")) {
                // 잘못된 형식 패턴 확인: /news/article/all/숫자
                Pattern wrongPattern = Pattern.compile("(https://www\\.donga\\.com)/news/article/all/(\\d+)");
                Matcher wrongMatcher = wrongPattern.matcher(news.getUrl());
                
                if (wrongMatcher.find()) {
                    String baseUrl = wrongMatcher.group(1);
                    String newsId = wrongMatcher.group(2);
                    
                    // 카테고리 결정 (뉴스 카테고리 기반)
                    String category = "";
                    if ("economy".equals(news.getCategory())) {
                        category = "Economy";
                    } else if ("sports".equals(news.getCategory())) {
                        category = "Sports";
                    } else {
                        category = "Inter"; // general은 Inter로 분류
                    }
                    
                    // 뉴스 생성 날짜 사용
                    String dateStr = news.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    
                    // 올바른 형식으로 URL 재구성
                    String correctUrl = baseUrl + "/news/" + category + "/article/all/" + dateStr + "/" + newsId + "/1";
                    
                    // URL 업데이트 (null이 아닌 경우에도 업데이트)
                    newsMapper.updateUrlIfNull(news.getNewsId(), correctUrl);
                    System.out.println("URL 형식 수정: " + news.getUrl() + " → " + correctUrl);
                    fixedCount++;
                }
            }
        }
        
        System.out.println("총 " + fixedCount + "개의 동아일보 URL 형식을 수정했습니다.");
        System.out.println("=== 동아일보 URL 형식 수정 완료 ===");
    }

    /**
     * 기존 뉴스들의 카테고리를 올바르게 업데이트
     */
    public void updateNewsCategories() {
        System.out.println("=== 기존 뉴스 카테고리 업데이트 시작 ===");
        
        // 모든 뉴스 조회
        List<News> allNews = newsMapper.findAllNews(0, 1000); // 최대 1000개
        System.out.println("총 뉴스 개수: " + allNews.size());
        
        for (News news : allNews) {
            String currentCategory = news.getCategory();
            String correctCategory = determineCorrectCategory(news);
            
            if (!currentCategory.equals(correctCategory)) {
                newsMapper.updateNewsCategory(news.getNewsId(), correctCategory);
                System.out.println("뉴스 ID " + news.getNewsId() + " 카테고리 업데이트: " + currentCategory + " → " + correctCategory);
            }
        }
        
        System.out.println("=== 기존 뉴스 카테고리 업데이트 완료 ===");
    }

    /**
     * 뉴스의 올바른 카테고리 결정 (제목과 내용 기반)
     */
    private String determineCorrectCategory(News news) {
        String title = news.getTitle().toLowerCase();
        String content = news.getContent().toLowerCase();
        String combined = title + " " + content;
        
        // 정치/사회 키워드 우선 체크 (스포츠보다 먼저)
        if (combined.contains("국힘") || combined.contains("민주") || combined.contains("정치") ||
            combined.contains("공무원") || combined.contains("사망") || combined.contains("폭행") ||
            combined.contains("법원") || combined.contains("재판") || combined.contains("범죄") ||
            combined.contains("경찰") || combined.contains("사건") || combined.contains("사고") ||
            combined.contains("정부") || combined.contains("대통령") || combined.contains("국회") ||
            combined.contains("선거") || combined.contains("국정") || combined.contains("외교") ||
            combined.contains("국제") || combined.contains("미국") || combined.contains("중국") ||
            combined.contains("일본") || combined.contains("유럽")) {
            return "general";
        }
        
        // 스포츠 키워드 (더 포괄적으로)
        if (combined.contains("축구") || combined.contains("야구") || combined.contains("농구") || 
            combined.contains("배구") || combined.contains("테니스") || combined.contains("골프") ||
            combined.contains("올림픽") || combined.contains("월드컵") || combined.contains("선수") ||
            combined.contains("경기") || combined.contains("팀") || combined.contains("리그") ||
            combined.contains("스포츠") || combined.contains("야구") || combined.contains("축구") ||
            combined.contains("농구") || combined.contains("배구") || combined.contains("테니스") ||
            combined.contains("골프") || combined.contains("수영") || combined.contains("체조") ||
            combined.contains("육상") || combined.contains("마라톤") || combined.contains("복싱") ||
            combined.contains("태권도") || combined.contains("유도") || combined.contains("레슬링")) {
            return "sports";
        }
        
        // 날씨/기상 관련 키워드 (스포츠가 아닌 general로 분류)
        if (combined.contains("날씨") || combined.contains("기상") || combined.contains("비") ||
            combined.contains("눈") || combined.contains("바람") || combined.contains("온도") ||
            combined.contains("습도") || combined.contains("강수") || combined.contains("태풍") ||
            combined.contains("호우") || combined.contains("폭설") || combined.contains("폭염") ||
            combined.contains("한파") || combined.contains("장마") || combined.contains("가뭄")) {
            return "general";
        }
        
        // 경제 키워드 (더 포괄적으로)
        if (combined.contains("주식") || combined.contains("증시") || combined.contains("경제") ||
            combined.contains("금리") || combined.contains("인플레이션") || combined.contains("부동산") ||
            combined.contains("기업") || combined.contains("매출") || combined.contains("수익") ||
            combined.contains("투자") || combined.contains("자산") || combined.contains("금융") ||
            combined.contains("은행") || combined.contains("보험") || combined.contains("증권") ||
            combined.contains("코스피") || combined.contains("코스닥") || combined.contains("환율") ||
            combined.contains("원화") || combined.contains("달러") || combined.contains("비트코인") ||
            combined.contains("암호화폐") || combined.contains("경기") || combined.contains("불황") ||
            combined.contains("호황") || combined.contains("경기침체") || combined.contains("경기회복")) {
            return "economy";
        }
        
        
        return "general"; // 기본값
    }

    /**
     * 카테고리별 기본 출처명 반환
     */
    private String getDefaultSourceByCategory(String category) {
        if (category == null) return "외부뉴스";
        
        switch (category.toLowerCase()) {
            case "sports":
                return "동아일보";
            case "economy":
                return "매일경제";
            case "tech":
                return "전자신문";
            case "general":
                return "연합뉴스";
            default:
                return "외부뉴스";
        }
    }
}