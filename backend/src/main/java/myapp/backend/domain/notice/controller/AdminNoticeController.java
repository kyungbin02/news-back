package myapp.backend.domain.notice.controller;

import myapp.backend.domain.notice.service.NoticeService;
import myapp.backend.domain.notice.vo.NoticeVO;
import myapp.backend.domain.auth.vo.UserPrincipal;
import myapp.backend.domain.admin.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notice")
public class AdminNoticeController {
    
    @Autowired
    private NoticeService noticeService;
    
    @Autowired
    private AdminService adminService;
    
    // 관리자용 공지사항 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<NoticeVO>> getAdminNoticeList(@AuthenticationPrincipal UserPrincipal principal) {
        try {
            // TODO: 관리자 권한 확인 로직 추가
            List<NoticeVO> noticeList = noticeService.getAdminNoticeList();
            return ResponseEntity.ok(noticeList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 관리자용 공지사항 상세 조회
    @GetMapping("/{notice_id}")
    public ResponseEntity<NoticeVO> getAdminNoticeDetail(
            @PathVariable("notice_id") int notice_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            // TODO: 관리자 권한 확인 로직 추가
            NoticeVO notice = noticeService.getAdminNoticeDetail(notice_id);
            return ResponseEntity.ok(notice);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 공지사항 작성
    @PostMapping("/create")
    public ResponseEntity<?> createNotice(
            @RequestBody NoticeVO notice,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            System.out.println("🔍 AdminNoticeController.createNotice 호출됨");
            System.out.println("👤 Principal: " + principal);
            System.out.println("📝 Request Body: " + notice);
            System.out.println("🔍 is_important 값 확인: " + notice.getIs_important());
            
            // TODO: 관리자 권한 확인 로직 추가
            if (principal != null) {
                // user_id로 admin_id를 조회
                int adminId = adminService.getAdminIdByUserId(principal.getUserId());
                if (adminId == 0) {
                    return ResponseEntity.badRequest().body("관리자 권한이 없습니다.");
                }
                notice.setAdmin_id(adminId);
                System.out.println("🔑 설정된 admin_id: " + notice.getAdmin_id());
            }
            
            noticeService.createNotice(notice);
            return ResponseEntity.ok().body("공지사항이 성공적으로 작성되었습니다.");
        } catch (Exception e) {
            System.err.println("❌ AdminNoticeController.createNotice 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("공지사항 작성에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 공지사항 수정
    @PutMapping("/{notice_id}")
    public ResponseEntity<?> updateNotice(
            @PathVariable("notice_id") int notice_id,
            @RequestBody NoticeVO notice,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            // TODO: 관리자 권한 확인 로직 추가
            notice.setNotice_id(notice_id);
            noticeService.updateNotice(notice);
            return ResponseEntity.ok().body("공지사항이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("공지사항 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 공지사항 삭제
    @DeleteMapping("/{notice_id}")
    public ResponseEntity<?> deleteNotice(
            @PathVariable("notice_id") int notice_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            // TODO: 관리자 권한 확인 로직 추가
            noticeService.deleteNotice(notice_id);
            return ResponseEntity.ok().body("공지사항이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("공지사항 삭제에 실패했습니다: " + e.getMessage());
        }
    }
}
