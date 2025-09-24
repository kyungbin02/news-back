package myapp.backend.domain.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, 
                                      AuthenticationException exception) throws IOException, ServletException {
        
        logger.error("OAuth2 로그인 실패: " + exception.getMessage(), exception);
        
        String errorUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/login")
                .queryParam("error", "oauth2_login_failed")
                .queryParam("message", exception.getMessage())
                .build().toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }
}



