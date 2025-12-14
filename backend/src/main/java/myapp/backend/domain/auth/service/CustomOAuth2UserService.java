package myapp.backend.domain.auth.service;

import myapp.backend.domain.auth.mapper.UserMapper;
import myapp.backend.domain.auth.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserMapper userMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        UserVO user = null;

        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            String snsId = (String) response.get("id");
            String email = (String) response.get("email");
            String username = (String) response.get("name"); // 변경부분 dd
            String profileImg = (String) response.get("profile_image");

            System.out.println("=== 네이버 로그인 처리 시작 ===");
            System.out.println("SNS ID: " + snsId);
            System.out.println("Username: " + username);
            System.out.println("Email: " + email);

            user = userMapper.findBySnsIdAndSnsType(snsId, registrationId);
            System.out.println("DB 조회 결과: " + (user == null ? "새로운 사용자" : "기존 사용자"));
            
            if (user == null) {
                // 새로운 사용자: INSERT
                System.out.println("새로운 사용자 생성 시작");
                user = new UserVO(registrationId, snsId, username, email, profileImg);
                System.out.println("INSERT 전 User ID: " + user.getUser_id());
                userMapper.save(user);
                System.out.println("INSERT 후 User ID: " + user.getUser_id());
                
                // INSERT 후 DB에서 직접 user_id 조회
                Integer dbUserId = userMapper.getUserIdBySnsInfo(snsId, registrationId);
                System.out.println("DB에서 직접 조회한 User ID: " + dbUserId);
                if (dbUserId != null) {
                    user.setUser_id(dbUserId);
                    System.out.println("강제 설정 후 User ID: " + user.getUser_id());
                }
                
                System.out.println("새로운 사용자 생성 완료 - User ID: " + user.getUser_id());
            } else {
                // 기존 사용자: UPDATE
                System.out.println("기존 사용자 업데이트 시작 - 기존 User ID: " + user.getUser_id());
                user.setUsername(username);
                user.setEmail(email);
                user.setProfile_img(profileImg);
                userMapper.updateUser(user);
                
                // UPDATE 후 DB에서 직접 user_id 조회하여 강제 설정
                Integer dbUserId = userMapper.getUserIdBySnsInfo(snsId, registrationId);
                System.out.println("기존 사용자 DB에서 직접 조회한 User ID: " + dbUserId);
                if (dbUserId != null) {
                    user.setUser_id(dbUserId);
                    System.out.println("기존 사용자 강제 설정 후 User ID: " + user.getUser_id());
                }
                
                System.out.println("기존 사용자 업데이트 완료 - User ID: " + user.getUser_id());
            }
            System.out.println("=== 네이버 로그인 처리 완료 ===");
            return new DefaultOAuth2User(oauth2User.getAuthorities(), attributes, "response");

        } else if ("kakao".equals(registrationId)) {
            String snsId = attributes.get("id").toString();

            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            String email = (String) kakaoAccount.get("email");

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            String username = (String) profile.get("nickname");
            String profileImg = (String) profile.get("profile_image_url");

            user = userMapper.findBySnsIdAndSnsType(snsId, registrationId);
            if (user == null) {
                user = new UserVO(registrationId, snsId, username, email, profileImg);
                userMapper.save(user);
                System.out.println("새로운 사용자 생성 - User ID: " + user.getUser_id());
            } else {
                user.setUsername(username);
                user.setEmail(email);
                user.setProfile_img(profileImg);
                userMapper.updateUser(user);
                System.out.println("기존 사용자 업데이트 - User ID: " + user.getUser_id());
            }
            return new DefaultOAuth2User(oauth2User.getAuthorities(), attributes, "id");

        } else if ("google".equals(registrationId)) {
            String snsId = (String) attributes.get("sub");
            String email = (String) attributes.get("email");
            String username = (String) attributes.get("name");
            String profileImg = (String) attributes.get("picture");

            user = userMapper.findBySnsIdAndSnsType(snsId, registrationId);
            if (user == null) {
                user = new UserVO(registrationId, snsId, username, email, profileImg);
                userMapper.save(user);
                System.out.println("새로운 사용자 생성 - User ID: " + user.getUser_id());
            } else {
                user.setUsername(username);
                user.setEmail(email);
                user.setProfile_img(profileImg);
                userMapper.updateUser(user);
                System.out.println("기존 사용자 업데이트 - User ID: " + user.getUser_id());
            }
            return new DefaultOAuth2User(oauth2User.getAuthorities(), attributes, "sub");
        }

        throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
    }
}
