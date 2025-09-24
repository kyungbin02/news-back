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

    // RSS 피드 URL들 (검증된 한국 언론사, 한글 뉴스)
    private final String[] RSS_URLS = {
        "https://rss.donga.com/total.xml",                  // 동아일보 전체 (한글)
        "https://rss.etnews.com/Section901.xml",           // 전자신문 뉴스 (한글)
        "https://rss.donga.com/economy.xml",                // 동아일보 경제 (한글)
        "https://rss.donga.com/sports.xml"                  // 동아일보 스포츠 (한글)
    };

    @Override
    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void collectRssNews() {
        System.out.println("RSS 뉴스 수집 시작: " + LocalDateTime.now());
        
        for (String rssUrl : RSS_URLS) {
            try {
                collectFromSingleRss(rssUrl);
            } catch (Exception e) {
                System.err.println("RSS 수집 실패: " + rssUrl + " - " + e.getMessage());
            }
        }
        
        System.out.println("RSS 뉴스 수집 완료: " + LocalDateTime.now());
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
                    newsMapper.insertNews(news);
                    System.out.println("새 뉴스 저장: " + news.getTitle());
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
        
        // HTML 태그 제거
        title = cleanHtmlTags(title);
        description = cleanHtmlTags(description);
        
        // 카테고리 결정
        String category = determineCategoryFromUrl(rssUrl);
        
        // 이미지 URL 추출 (description과 item 전체에서 시도)
        String imageUrl = extractImageUrl(description);
        if (imageUrl == null) {
            // description에서 못찾으면 item 전체에서 시도
            imageUrl = extractImageFromItem(item);
        }
        
        news.setTitle(title);
        news.setContent(description);  // content에 description 저장
        news.setCategory(category);
        news.setImageUrl(imageUrl);
        news.setViews(0);
        
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
        if (rssUrl.contains("sports")) return "sports";              // 스포츠
        if (rssUrl.contains("economy")) return "economy";            // 경제
        if (rssUrl.contains("politics")) return "politics";          // 정치
        if (rssUrl.contains("etnews")) return "tech";                // 전자신문 (IT/기술)
        if (rssUrl.contains("total")) return "general";              // 동아일보 전체
        return "general";
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
    public News getNewsById(int newsId) {
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
}