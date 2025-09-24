package myapp.backend.domain.admin.adminboard.service;

import myapp.backend.domain.admin.adminboard.mapper.AdminBoardMapper;
import myapp.backend.domain.admin.adminboard.vo.AdminBoardVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminBoardServiceImpl implements AdminBoardService {
    
    @Autowired
    private AdminBoardMapper adminBoardMapper;
    
    @Override
    public List<AdminBoardVO> getAdminBoardList() {
        List<AdminBoardVO> boardList = adminBoardMapper.getAdminBoardList();
        
        // image_url을 imageUrls로 파싱
        for (AdminBoardVO board : boardList) {
            if (board.getImage_url() != null && !board.getImage_url().isEmpty()) {
                String[] urls = board.getImage_url().split(",");
                board.setImageUrls(java.util.Arrays.asList(urls));
            }
        }
        
        return boardList;
    }
    
    @Override
    public AdminBoardVO getAdminBoardDetail(int board_id) {
        AdminBoardVO board = adminBoardMapper.getAdminBoardDetail(board_id);
        
        // image_url을 imageUrls로 파싱
        if (board != null && board.getImage_url() != null && !board.getImage_url().isEmpty()) {
            String[] urls = board.getImage_url().split(",");
            board.setImageUrls(java.util.Arrays.asList(urls));
        }
        
        return board;
    }
    
    @Override
    @Transactional
    public boolean deleteAdminBoard(int board_id) {
        try {
            int result = adminBoardMapper.deleteAdminBoard(board_id);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
