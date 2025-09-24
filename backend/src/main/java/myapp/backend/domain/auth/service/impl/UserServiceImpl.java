package myapp.backend.domain.auth.service.impl;

import myapp.backend.domain.auth.mapper.UserMapper;
import myapp.backend.domain.auth.service.UserService;
import myapp.backend.domain.auth.vo.UserVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserVO processOAuth2User(String sns_type, String sns_id, String username, String email, String profile_img) {
        UserVO user = userMapper.findBySnsIdAndSnsType(sns_id, sns_type);

        if (user == null) {
            // 새로운 사용자: 회원가입 처리
            user = new UserVO(sns_type, sns_id, username, email, profile_img);
            userMapper.save(user);
        } else {
            // 기존 사용자: 정보 업데이트 (필요하다면)
            // 예: 사용자 이름이나 프로필 이미지가 변경될 수 있으므로 업데이트
            user.setUsername(username);
            user.setEmail(email);
            user.setProfile_img(profile_img);
            userMapper.updateUser(user); // 기존 사용자 정보 업데이트
        }
        return user;
    }
    
    // <경빈> 사용자 ID로 사용자 정보 조회 구현
    @Override
    public UserVO getUserById(Integer userId) {
        try {
            System.out.println("=== 데이터베이스 사용자 조회 시작 ===");
            System.out.println("조회할 userId: " + userId);
            
            UserVO user = userMapper.findByUserId(userId);
            System.out.println("DB 조회 결과: " + user);
            
            if (user != null) {
                System.out.println("사용자 정보 조회 성공: " + user.getUsername());
            } else {
                System.out.println("사용자 정보 없음: userId=" + userId);
            }
            
            return user;
        } catch (Exception e) {
            System.out.println("데이터베이스 조회 오류: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 