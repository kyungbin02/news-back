package myapp.backend.domain.inquiry.mapper;

import myapp.backend.domain.inquiry.vo.InquiryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface InquiryMapper {
    // 사용자용 문의사항 목록 조회 (본인 것만)
    List<InquiryVO> getInquiryListByUserId(@Param("user_id") int user_id);
    
    // 사용자용 문의사항 상세 조회 (본인 것만)
    InquiryVO getInquiryDetailByUserId(@Param("inquiry_id") int inquiry_id, @Param("user_id") int user_id);
    
    // 관리자용 문의사항 목록 조회 (전체)
    List<InquiryVO> getAdminInquiryList();
    
    // 관리자용 문의사항 상세 조회
    InquiryVO getAdminInquiryDetail(@Param("inquiry_id") int inquiry_id);
    
    // 문의사항 작성
    void insertInquiry(InquiryVO inquiry);
    
    // 문의사항 수정 (답변 전까지만)
    int updateInquiry(InquiryVO inquiry);
    
    // 문의사항 삭제 (답변 전까지만)
    int deleteInquiry(@Param("inquiry_id") int inquiry_id, @Param("user_id") int user_id);
    
    // 문의사항 상태 업데이트
    int updateInquiryStatus(@Param("inquiry_id") int inquiry_id, @Param("status") String status);
    
    // 답변 여부 확인
    boolean hasAnswer(@Param("inquiry_id") int inquiry_id);
    
    // 공개용 문의사항 목록 조회 (인증 없이)
    List<InquiryVO> getPublicInquiryList();
    
    // 공개용 문의사항 상세 조회 (인증 없이)
    InquiryVO getPublicInquiryDetail(@Param("inquiry_id") int inquiry_id);
    
    // 문의사항 ID로 조회 (알림용)
    InquiryVO getInquiryById(@Param("inquiry_id") int inquiry_id);

}
