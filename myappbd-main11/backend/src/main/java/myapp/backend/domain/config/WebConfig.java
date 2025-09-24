package myapp.backend.domain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000") // 프론트엔드 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // 쿠키 허용
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /upload/** 경로를 정적 리소스로 매핑 (실제 파일 시스템 경로 사용)
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:src/main/resources/static/upload/")
                .setCachePeriod(3600) // 캐시 설정
                .resourceChain(true); // 리소스 체인 활성화
        
        // 추가적인 정적 리소스 매핑
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
