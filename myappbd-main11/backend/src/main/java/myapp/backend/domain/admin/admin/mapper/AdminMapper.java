package myapp.backend.domain.admin.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import myapp.backend.domain.admin.admin.vo.AdminVO;

@Mapper
public interface AdminMapper {
    
    // 관리자 정보 조회 (username 포함)
    AdminVO getAdminInfo(@Param("user_id") int user_id);
    
    // 관리자 권한 확인
    boolean isAdmin(@Param("user_id") int user_id);
}
