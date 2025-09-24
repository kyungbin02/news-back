package myapp.backend.domain.admin.admin.service;

import myapp.backend.domain.admin.admin.mapper.AdminMapper;
import myapp.backend.domain.admin.admin.vo.AdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Override
    public AdminVO getAdminInfo(int userId) {
        return adminMapper.getAdminInfo(userId);
    }
    
    @Override
    public boolean isAdmin(int userId) {
        return adminMapper.isAdmin(userId);
    }
    
    @Override
    public int getAdminIdByUserId(int userId) {
        AdminVO admin = adminMapper.getAdminInfo(userId);
        return admin != null ? admin.getAdmin_id() : 0;
    }
}


