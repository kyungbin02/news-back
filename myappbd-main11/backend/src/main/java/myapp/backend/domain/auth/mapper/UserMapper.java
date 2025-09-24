package myapp.backend.domain.auth.mapper;

import myapp.backend.domain.auth.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    UserVO findBySnsIdAndSnsType(@Param("sns_id") String sns_id, @Param("sns_type") String sns_type);

    void save(UserVO user);

    void updateUser(UserVO user);

    UserVO findByUserId(@Param("user_id") int user_id);

    // DB에서 직접 user_id 조회
    Integer getUserIdBySnsInfo(@Param("sns_id") String sns_id, @Param("sns_type") String sns_type);
    
    // 사용자 상태 업데이트 (제재 해제용)
    void updateUserStatus(@Param("user_id") int user_id, 
                         @Param("user_status") String user_status,
                         @Param("sanction_reason") String sanction_reason,
                         @Param("sanction_start_date") String sanction_start_date,
                         @Param("sanction_end_date") String sanction_end_date);
    
    // 만료된 제재 자동 해제 (스케줄러용)
    int updateExpiredSanctions();
    
    // 사용자 상태 조회 (제재 정보 포함)
    UserVO getUserStatus(@Param("user_id") int user_id);
    
    // 전체 사용자 목록 조회 (관리자용)
    List<UserVO> getAllUsers(@Param("offset") int offset, @Param("limit") int limit);
    
    // 전체 사용자 수 조회
    int getTotalUserCount();
    
    // 제재된 사용자 목록 조회 (관리자용)
    List<UserVO> getSanctionedUsers(@Param("offset") int offset, @Param("limit") int limit);
    
    // 제재된 사용자 수 조회
    int getSanctionedUserCount();
} 