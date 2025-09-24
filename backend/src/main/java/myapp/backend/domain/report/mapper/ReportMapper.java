package myapp.backend.domain.report.mapper;

import myapp.backend.domain.report.vo.ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReportMapper {
    
    // 신고 등록
    void insertReport(ReportVO report);
    
    // 내 신고 목록 조회
    List<ReportVO> getMyReports(@Param("reporter_id") int reporter_id);
    
    // 신고 상세 조회 (신고된 내용 포함)
    ReportVO getReportDetail(@Param("report_id") int report_id);
    
    // 관리자용 신고 목록 조회
    List<ReportVO> getAdminReportList();
    
    // 신고 상태 업데이트
    int updateReportStatus(@Param("report_id") int report_id, 
                          @Param("admin_id") Integer admin_id, 
                          @Param("admin_comment") String admin_comment, 
                          @Param("status") String status);
    
    // 사용자 제재 적용
    int applyUserSanction(@Param("user_id") int user_id, 
                         @Param("sanction_reason") String sanction_reason, 
                         @Param("sanction_days") int sanction_days);
    
    // 사용자 경고 적용
    int applyUserWarning(@Param("user_id") int user_id, 
                        @Param("sanction_reason") String sanction_reason);
}
