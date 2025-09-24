package myapp.backend.domain.report.service;

import myapp.backend.domain.report.vo.ReportVO;
import myapp.backend.domain.report.vo.ReportRequest;
import myapp.backend.domain.report.vo.SanctionRequest;
import java.util.List;

public interface ReportService {
    
    // 사용자 신고하기
    void reportUser(ReportRequest request, int reporter_id);
    
    // 내 신고 목록 조회
    List<ReportVO> getMyReports(int reporter_id);
    
    // 신고 상세 조회 (신고된 내용 포함)
    ReportVO getReportDetail(int report_id);
    
    // 관리자용 신고 목록 조회
    List<ReportVO> getAdminReportList();
    
    // 신고 처리 (제재 적용)
    void processReport(int report_id, SanctionRequest request, Integer admin_id);
}
