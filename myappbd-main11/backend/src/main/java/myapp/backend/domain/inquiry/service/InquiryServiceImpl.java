package myapp.backend.domain.inquiry.service;

import myapp.backend.domain.inquiry.mapper.InquiryMapper;
import myapp.backend.domain.inquiry.vo.InquiryVO;
import myapp.backend.domain.notification.service.NotificationService;
import myapp.backend.domain.auth.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InquiryServiceImpl implements InquiryService {
    
    @Autowired
    private InquiryMapper inquiryMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public List<InquiryVO> getInquiryListByUserId(int user_id) {
        return inquiryMapper.getInquiryListByUserId(user_id);
    }
    
    @Override
    public InquiryVO getInquiryDetailByUserId(int inquiry_id, int user_id) {
        InquiryVO inquiry = inquiryMapper.getInquiryDetailByUserId(inquiry_id, user_id);
        if (inquiry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문의사항을 찾을 수 없습니다.");
        }
        return inquiry;
    }
    
    @Override
    public List<InquiryVO> getAdminInquiryList() {
        return inquiryMapper.getAdminInquiryList();
    }
    
    @Override
    public InquiryVO getAdminInquiryDetail(int inquiry_id) {
        InquiryVO inquiry = inquiryMapper.getAdminInquiryDetail(inquiry_id);
        if (inquiry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문의사항을 찾을 수 없습니다.");
        }
        return inquiry;
    }
    
    @Override
    @Transactional
    public void createInquiry(InquiryVO inquiry) {
        try {
            inquiry.setInquiry_status("pending");
            inquiryMapper.insertInquiry(inquiry);
            
            // 문의사항 등록 알림 전송
            try {
                String inquirerUsername = userMapper.findByUserId(inquiry.getUser_id()).getUsername();
                notificationService.notifyInquiry(
                    inquiry.getInquiry_id(), 
                    inquiry.getInquiry_title(), 
                    inquirerUsername
                );
                System.out.println("[InquiryServiceImpl] 문의사항 등록 알림 전송 완료 - inquiryId: " + inquiry.getInquiry_id());
            } catch (Exception e) {
                System.err.println("[InquiryServiceImpl] 문의사항 등록 알림 전송 실패: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "문의사항 작성에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void updateInquiry(InquiryVO inquiry) {
        try {
            // 답변 여부 확인
            if (inquiryMapper.hasAnswer(inquiry.getInquiry_id())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "답변이 완료된 문의사항은 수정할 수 없습니다.");
            }
            
            int result = inquiryMapper.updateInquiry(inquiry);
            if (result == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문의사항을 찾을 수 없거나 수정할 수 없습니다.");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "문의사항 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void deleteInquiry(int inquiry_id, int user_id) {
        try {
            // 답변 여부 확인
            if (inquiryMapper.hasAnswer(inquiry_id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "답변이 완료된 문의사항은 삭제할 수 없습니다.");
            }
            
            int result = inquiryMapper.deleteInquiry(inquiry_id, user_id);
            if (result == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문의사항을 찾을 수 없거나 삭제할 수 없습니다.");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "문의사항 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public List<InquiryVO> getPublicInquiryList() {
        return inquiryMapper.getPublicInquiryList();
    }
    
    @Override
    public InquiryVO getPublicInquiryDetail(int inquiry_id) {
        InquiryVO inquiry = inquiryMapper.getPublicInquiryDetail(inquiry_id);
        if (inquiry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "문의사항을 찾을 수 없습니다.");
        }
        return inquiry;
    }
    

}
