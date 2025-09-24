package myapp.backend.domain.inquiry.service;

import myapp.backend.domain.inquiry.vo.InquiryVO;
import java.util.List;

public interface InquiryService {
    // 사용자용 문의사항 목록 조회 (본인 것만)
    List<InquiryVO> getInquiryListByUserId(int user_id);
    
    // 사용자용 문의사항 상세 조회 (본인 것만)
    InquiryVO getInquiryDetailByUserId(int inquiry_id, int user_id);
    
    // 관리자용 문의사항 목록 조회 (전체)
    List<InquiryVO> getAdminInquiryList();
    
    // 관리자용 문의사항 상세 조회
    InquiryVO getAdminInquiryDetail(int inquiry_id);
    
    // 문의사항 작성
    void createInquiry(InquiryVO inquiry);
    
    // 문의사항 수정 (답변 전까지만)
    void updateInquiry(InquiryVO inquiry);
    
    // 문의사항 삭제 (답변 전까지만)
    void deleteInquiry(int inquiry_id, int user_id);
    
    // 공개용 문의사항 목록 조회 (인증 없이)
    List<InquiryVO> getPublicInquiryList();
    
    // 공개용 문의사항 상세 조회 (인증 없이)
    InquiryVO getPublicInquiryDetail(int inquiry_id);
    

}
