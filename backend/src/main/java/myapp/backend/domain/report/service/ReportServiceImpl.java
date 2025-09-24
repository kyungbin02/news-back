package myapp.backend.domain.report.service;

import myapp.backend.domain.report.mapper.ReportMapper;
import myapp.backend.domain.report.vo.ReportVO;
import myapp.backend.domain.report.vo.ReportRequest;
import myapp.backend.domain.report.vo.SanctionRequest;
import myapp.backend.domain.notification.service.NotificationService;
import myapp.backend.domain.auth.mapper.UserMapper;
import myapp.backend.domain.board.mapper.BoardMapper;
import myapp.backend.domain.board.mapper.BoardCommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    
    @Autowired
    private ReportMapper reportMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BoardMapper boardMapper;
    
    @Autowired
    private BoardCommentMapper boardCommentMapper;
    
    @Override
    @Transactional
    public void reportUser(ReportRequest request, int reporter_id) {
        try {
            ReportVO report = new ReportVO();
            report.setReporter_id(reporter_id);
            report.setReported_user_id(request.getReported_user_id());
            report.setReport_reason(request.getReport_reason());
            report.setReport_content(request.getReport_content());
            report.setTarget_type(request.getTarget_type());
            report.setTarget_id(request.getTarget_id());
            report.setReport_status("pending");
            
            reportMapper.insertReport(report);
            
            // 신고 알림 전송
            try {
                String reporterUsername = userMapper.findByUserId(reporter_id).getUsername();
                
                if ("board".equals(request.getTarget_type()) && request.getTarget_id() != null) {
                    // 게시글 신고 알림
                    var board = boardMapper.getBoardDetailById(request.getTarget_id());
                    if (board != null) {
                        notificationService.notifyReportBoard(
                            request.getTarget_id(), 
                            board.getTitle(), 
                            reporterUsername
                        );
                        System.out.println("[ReportServiceImpl] 게시글 신고 알림 전송 완료 - boardId: " + request.getTarget_id());
                    }
                } else if ("board_comment".equals(request.getTarget_type()) && request.getTarget_id() != null) {
                    // 댓글 신고 알림
                    var comment = boardCommentMapper.getCommentById(request.getTarget_id());
                    if (comment != null) {
                        notificationService.notifyReportComment(
                            request.getTarget_id(), 
                            comment.getComment_content(), 
                            reporterUsername
                        );
                        System.out.println("[ReportServiceImpl] 댓글 신고 알림 전송 완료 - commentId: " + request.getTarget_id());
                    }
                }
            } catch (Exception e) {
                System.err.println("[ReportServiceImpl] 신고 알림 전송 실패: " + e.getMessage());
            }
            
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "신고 등록에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public List<ReportVO> getMyReports(int reporter_id) {
        return reportMapper.getMyReports(reporter_id);
    }
    
    @Override
    public ReportVO getReportDetail(int report_id) {
        ReportVO report = reportMapper.getReportDetail(report_id);
        if (report == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다.");
        }
        return report;
    }
    
    @Override
    public List<ReportVO> getAdminReportList() {
        return reportMapper.getAdminReportList();
    }
    
    @Override
    @Transactional
    public void processReport(int report_id, SanctionRequest request, Integer admin_id) {
        try {
            // 신고 상세 조회
            ReportVO report = getReportDetail(report_id);
            int reported_user_id = report.getReported_user_id();
            
            // action에 따른 처리
            if ("reject".equals(request.getAction())) {
                // 반려: 제재 없이 상태만 변경
                String adminComment = request.getAdmin_comment() != null ? request.getAdmin_comment() : "반려 처리됨";
                reportMapper.updateReportStatus(report_id, admin_id, adminComment, "rejected");
            } else if ("sanction".equals(request.getAction())) {
                // 제재: 제재 적용 후 상태 변경
                
                // sanctionType이 null이면 기본값으로 "suspended_7days" 설정
                String sanctionType = request.getSanctionType();
                if (sanctionType == null || sanctionType.trim().isEmpty()) {
                    sanctionType = "suspended_7days";
                }
                
                int sanctionDays = getSanctionDays(sanctionType);
                
                String status;
                if (sanctionDays > 0) {
                    // 7일 정지
                    String sanctionReason = "suspended_7days".equals(sanctionType) ? 
                        "부적절한 행동으로 7일 정지" : request.getAdmin_comment();
                    reportMapper.applyUserSanction(reported_user_id, sanctionReason, sanctionDays);
                    status = "suspended";  // 7일 정지
                } else {
                    // 경고만
                    String warningReason = "warning".equals(sanctionType) ? 
                        "경고 조치" : request.getAdmin_comment();
                    reportMapper.applyUserWarning(reported_user_id, warningReason);
                    status = "warning";    // 경고
                }
                
                // 신고 상태를 제재 유형에 따라 변경
                String adminComment = request.getAdmin_comment() != null ? request.getAdmin_comment() : "제재 처리됨";
                reportMapper.updateReportStatus(report_id, admin_id, adminComment, status);
            }
            
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "신고 처리에 실패했습니다: " + e.getMessage());
        }
    }
    
    private int getSanctionDays(String sanctionType) {
        if (sanctionType == null || sanctionType.trim().isEmpty()) {
            return 0;  // null이거나 빈 문자열이면 기본값 0 반환
        }
        
        switch (sanctionType) {
            case "warning": return 0;           // 경고 (제재 없음)
            case "suspended_7days": return 7;   // 7일 정지
            default: return 0;
        }
    }
}
