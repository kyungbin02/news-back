package myapp.backend.domain.inquiry.service;

import myapp.backend.domain.inquiry.vo.InquiryAnswerVO;

public interface InquiryAnswerService {
    // 답변 조회 (개별)
    InquiryAnswerVO getAnswer(int answer_id);
    
    // 답변 조회 (문의사항별)
    InquiryAnswerVO getAnswerByInquiryId(int inquiry_id);
    
    // 답변 작성
    void createAnswer(InquiryAnswerVO answer);
    
    // 답변 수정
    void updateAnswer(InquiryAnswerVO answer);
    
    // 답변 삭제
    void deleteAnswer(int answer_id);
}
