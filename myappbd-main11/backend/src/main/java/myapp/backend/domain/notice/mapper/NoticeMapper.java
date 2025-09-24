package myapp.backend.domain.notice.mapper;

import myapp.backend.domain.notice.vo.NoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NoticeMapper {
    
    // 공지사항 목록 조회 (사용자용)
    List<NoticeVO> getNoticeList();
    
    // 공지사항 상세 조회 (사용자용)
    NoticeVO getNoticeDetail(@Param("notice_id") int notice_id);
    
    // 조회수 증가
    void increaseViewCount(@Param("notice_id") int notice_id);
    
    // 공지사항 작성 (관리자용)
    void insertNotice(NoticeVO notice);
    
    // 공지사항 수정 (관리자용)
    int updateNotice(NoticeVO notice);
    
    // 공지사항 삭제 (관리자용)
    int deleteNotice(@Param("notice_id") int notice_id);
    
    // 관리자용 공지사항 목록 조회
    List<NoticeVO> getAdminNoticeList();
    
    // 관리자용 공지사항 상세 조회
    NoticeVO getAdminNoticeDetail(@Param("notice_id") int notice_id);
}
