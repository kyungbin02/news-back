package myapp.backend.domain.inquiry.controller;

import myapp.backend.domain.inquiry.service.InquiryService;
import myapp.backend.domain.inquiry.vo.InquiryVO;
import myapp.backend.domain.auth.vo.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Base64;

@RestController
@RequestMapping("/api/inquiry")
public class InquiryController {
    
    @Autowired
    private InquiryService inquiryService;
    
    // Base64 디코딩을 안전하게 처리하는 헬퍼 메서드
    private String safeBase64Decode(String encodedString) {
        if (encodedString == null || encodedString.trim().isEmpty()) {
            return "";
        }
        try {
            // Base64 패딩 추가 (필요한 경우)
            String padded = encodedString;
            while (padded.length() % 4 != 0) {
                padded += "=";
            }
            // URL 안전한 Base64를 표준 Base64로 변환
            padded = padded.replace('-', '+').replace('_', '/');
            byte[] decodedBytes = Base64.getDecoder().decode(padded);
            return new String(decodedBytes, "UTF-8");
        } catch (Exception e) {
            // 디코딩 실패 시 원본 문자열 반환
            return encodedString;
        }
    }
    
    // 내 문의사항 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<InquiryVO>> getInquiryList(@AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(null);
            }
            List<InquiryVO> inquiryList = inquiryService.getInquiryListByUserId(principal.getUserId());
            return ResponseEntity.ok(inquiryList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 내 문의사항 상세 조회
    @GetMapping("/{inquiry_id}")
    public ResponseEntity<InquiryVO> getInquiryDetail(
            @PathVariable("inquiry_id") int inquiry_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(null);
            }
            InquiryVO inquiry = inquiryService.getInquiryDetailByUserId(inquiry_id, principal.getUserId());
            return ResponseEntity.ok(inquiry);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 문의사항 작성
    @PostMapping("/create")
    public ResponseEntity<?> createInquiry(
            @RequestBody InquiryVO inquiry,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }
            inquiry.setUser_id(principal.getUserId());
            inquiryService.createInquiry(inquiry);
            return ResponseEntity.ok().body("문의사항이 성공적으로 작성되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("문의사항 작성에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 문의사항 수정
    @PutMapping("/{inquiry_id}")
    public ResponseEntity<?> updateInquiry(
            @PathVariable("inquiry_id") int inquiry_id,
            @RequestBody InquiryVO inquiry,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }
            inquiry.setInquiry_id(inquiry_id);
            inquiry.setUser_id(principal.getUserId());
            inquiryService.updateInquiry(inquiry);
            return ResponseEntity.ok().body("문의사항이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("문의사항 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 문의사항 삭제
    @DeleteMapping("/{inquiry_id}")
    public ResponseEntity<?> deleteInquiry(
            @PathVariable("inquiry_id") int inquiry_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.badRequest().body("로그인이 필요합니다.");
            }
            inquiryService.deleteInquiry(inquiry_id, principal.getUserId());
            return ResponseEntity.ok().body("문의사항이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("문의사항 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    

}
