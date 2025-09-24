package myapp.backend.domain.notice.service;

import myapp.backend.domain.notice.vo.NoticeVO;
import java.util.List;

public interface NoticeService {
    
    // 사용자용 공지사항 목록 조회
    List<NoticeVO> getNoticeList();
    
    // 사용자용 공지사항 상세 조회
    NoticeVO getNoticeDetail(int notice_id);
    
    // 관리자용 공지사항 목록 조회
    List<NoticeVO> getAdminNoticeList();
    
    // 관리자용 공지사항 상세 조회
    NoticeVO getAdminNoticeDetail(int notice_id);
    
    // 공지사항 작성 (관리자용)
    void createNotice(NoticeVO notice);
    
    // 공지사항 수정 (관리자용)
    void updateNotice(NoticeVO notice);
    
    // 공지사항 삭제 (관리자용)
    void deleteNotice(int notice_id);
}
