package myapp.backend.domain.notice.service;

import myapp.backend.domain.notice.mapper.NoticeMapper;
import myapp.backend.domain.notice.vo.NoticeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {
    
    @Autowired
    private NoticeMapper noticeMapper;
    
    @Override
    public List<NoticeVO> getNoticeList() {
        return noticeMapper.getNoticeList();
    }
    
    @Override
    public NoticeVO getNoticeDetail(int notice_id) {
        System.out.println("🔍 NoticeServiceImpl.getNoticeDetail 호출됨 - notice_id: " + notice_id);
        
        NoticeVO notice = noticeMapper.getNoticeDetail(notice_id);
        if (notice == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다.");
        }
        
        // 조회수 증가
        System.out.println("📊 조회수 증가 전: " + notice.getView_count());
        noticeMapper.increaseViewCount(notice_id);
        System.out.println("📊 조회수 증가 완료");
        
        return notice;
    }
    
    @Override
    public List<NoticeVO> getAdminNoticeList() {
        return noticeMapper.getAdminNoticeList();
    }
    
    @Override
    public NoticeVO getAdminNoticeDetail(int notice_id) {
        NoticeVO notice = noticeMapper.getAdminNoticeDetail(notice_id);
        if (notice == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다.");
        }
        
        // 조회수 증가
        noticeMapper.increaseViewCount(notice_id);
        
        return notice;
    }
    
    @Override
    @Transactional
    public void createNotice(NoticeVO notice) {
        try {
            System.out.println("🔍 NoticeServiceImpl.createNotice 호출됨");
            System.out.println("📝 입력 데이터: " + notice);
            System.out.println("🔍 is_important 값: " + notice.isIs_important());
            System.out.println("🔍 is_important 타입: " + (notice.isIs_important() ? "true" : "false"));
            noticeMapper.insertNotice(notice);
            System.out.println("✅ 공지사항 작성 성공");
        } catch (Exception e) {
            System.err.println("❌ 공지사항 작성 실패: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "공지사항 작성에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void updateNotice(NoticeVO notice) {
        try {
            int result = noticeMapper.updateNotice(notice);
            if (result == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "공지사항 수정에 실패했습니다.");
        }
    }
    
    @Override
    @Transactional
    public void deleteNotice(int notice_id) {
        try {
            int result = noticeMapper.deleteNotice(notice_id);
            if (result == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "공지사항 삭제에 실패했습니다.");
        }
    }
}
