package myapp.backend.domain.auth.service;

import myapp.backend.domain.auth.vo.UserVO;

public interface UserService {
    // 소셜 로그인 사용자 정보 처리 및 저장/업데이트
    UserVO processOAuth2User(String sns_type, String sns_id, String username, String email, String profile_img);

    // <경빈> 사용자 ID로 사용자 정보 조회
    UserVO getUserById(Integer userId);

    // 기타 필요한 사용자 관련 메소드 (예: 일반 회원가입, 로그인 등)
    // UserVO registerUser(UserVO user);
    // UserVO loginUser(String email, String password);
}
