package myapp.backend.domain.admin.admin.service;

import myapp.backend.domain.admin.admin.vo.AdminVO;

public interface AdminService {
    AdminVO getAdminInfo(int userId);
    boolean isAdmin(int userId);
    int getAdminIdByUserId(int userId); // 추가: user_id로 admin_id 조회
}


