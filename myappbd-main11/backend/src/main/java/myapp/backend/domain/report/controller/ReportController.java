package myapp.backend.domain.report.controller;

import myapp.backend.domain.report.service.ReportService;
import myapp.backend.domain.report.vo.ReportVO;
import myapp.backend.domain.report.vo.ReportRequest;
import myapp.backend.domain.report.vo.SanctionRequest;
import myapp.backend.domain.auth.vo.UserPrincipal;
import myapp.backend.domain.admin.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private AdminService adminService;
    
    // 사용자 신고하기
    @PostMapping("/user")
    public ResponseEntity<?> reportUser(
            @RequestBody ReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }
            
            reportService.reportUser(request, principal.getUserId());
            return ResponseEntity.ok().body("신고가 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("신고 등록에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 내 신고 목록 조회
    @GetMapping("/my")
    public ResponseEntity<List<ReportVO>> getMyReports(@AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(null);
            }
            
            List<ReportVO> reports = reportService.getMyReports(principal.getUserId());
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 신고 상세 조회 (신고된 내용 포함)
    @GetMapping("/{report_id}")
    public ResponseEntity<ReportVO> getReportDetail(
            @PathVariable("report_id") int report_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(null);
            }
            
            ReportVO report = reportService.getReportDetail(report_id);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 관리자용 신고 목록 조회
    @GetMapping("/admin/list")
    public ResponseEntity<List<ReportVO>> getAdminReportList(@AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(null);
            }
            
            // 관리자 권한 체크
            if (!isAdmin(principal)) {
                return ResponseEntity.status(403).body(null);
            }
            
            List<ReportVO> reports = reportService.getAdminReportList();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 신고 처리 (제재 적용) - 관리자용
    @PostMapping("/admin/{report_id}/process")
    public ResponseEntity<?> processReport(
            @PathVariable("report_id") int report_id,
            @RequestBody SanctionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }
            
            // 관리자 권한 체크
            if (!isAdmin(principal)) {
                return ResponseEntity.status(403).body("관리자 권한이 필요합니다.");
            }
            
            // 실제 admin_id를 가져오는 로직
            Integer adminId = adminService.getAdminIdByUserId(principal.getUserId());
            reportService.processReport(report_id, request, adminId);
            return ResponseEntity.ok().body("신고 처리가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("신고 처리에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 관리자 권한 체크 메서드
    private boolean isAdmin(UserPrincipal principal) {
        return adminService.isAdmin(principal.getUserId());
    }
}
