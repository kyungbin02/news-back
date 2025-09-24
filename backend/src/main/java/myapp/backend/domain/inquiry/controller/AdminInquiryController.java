package myapp.backend.domain.inquiry.controller;

import myapp.backend.domain.inquiry.service.InquiryService;
import myapp.backend.domain.inquiry.service.InquiryAnswerService;
import myapp.backend.domain.inquiry.vo.InquiryVO;
import myapp.backend.domain.inquiry.vo.InquiryAnswerVO;
import myapp.backend.domain.auth.vo.UserPrincipal;
import myapp.backend.domain.admin.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inquiry")
public class AdminInquiryController {
    
    @Autowired
    private InquiryService inquiryService;
    
    @Autowired
    private InquiryAnswerService inquiryAnswerService;
    
    @Autowired
    private AdminService adminService;
    
    // 관리자용 문의사항 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<InquiryVO>> getAdminInquiryList(@AuthenticationPrincipal UserPrincipal principal) {
        try {
            // TODO: 관리자 권한 확인 로직 추가
            List<InquiryVO> inquiryList = inquiryService.getAdminInquiryList();
            return ResponseEntity.ok(inquiryList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 관리자용 문의사항 상세 조회
    @GetMapping("/{inquiry_id}")
    public ResponseEntity<InquiryVO> getAdminInquiryDetail(
            @PathVariable("inquiry_id") int inquiry_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            // TODO: 관리자 권한 확인 로직 추가
            InquiryVO inquiry = inquiryService.getAdminInquiryDetail(inquiry_id);
            return ResponseEntity.ok(inquiry);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 문의사항 답변 작성
    @PostMapping("/{inquiry_id}/answer")
    public ResponseEntity<?> createAnswer(
            @PathVariable("inquiry_id") int inquiry_id,
            @RequestBody InquiryAnswerVO answer,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            // TODO: 관리자 권한 확인 로직 추가
            if (principal == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }
            
            // user_id로 admin_id 조회
            int adminId = adminService.getAdminIdByUserId(principal.getUserId());
            if (adminId == 0) {
                return ResponseEntity.badRequest().body("관리자 권한이 없습니다.");
            }
            
            answer.setInquiry_id(inquiry_id);
            answer.setAdmin_id(adminId);
            inquiryAnswerService.createAnswer(answer);
            return ResponseEntity.ok().body("답변이 성공적으로 작성되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("답변 작성에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 문의사항 답변 수정
    @PutMapping("/answer/{answer_id}")
    public ResponseEntity<?> updateAnswer(
            @PathVariable("answer_id") int answer_id,
            @RequestBody InquiryAnswerVO answer,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            // TODO: 관리자 권한 확인 로직 추가
            if (principal == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }
            
            answer.setAnswer_id(answer_id);
            inquiryAnswerService.updateAnswer(answer);
            return ResponseEntity.ok().body("답변이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("답변 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 문의사항 답변 삭제
    @DeleteMapping("/answer/{answer_id}")
    public ResponseEntity<?> deleteAnswer(
            @PathVariable("answer_id") int answer_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            // TODO: 관리자 권한 확인 로직 추가
            if (principal == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }
            
            inquiryAnswerService.deleteAnswer(answer_id);
            return ResponseEntity.ok().body("답변이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("답변 삭제에 실패했습니다: " + e.getMessage());
        }
    }
}
