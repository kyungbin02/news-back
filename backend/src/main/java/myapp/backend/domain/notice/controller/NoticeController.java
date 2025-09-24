package myapp.backend.domain.notice.controller;

import myapp.backend.domain.notice.service.NoticeService;
import myapp.backend.domain.notice.vo.NoticeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {
    
    @Autowired
    private NoticeService noticeService;
    
    // 공지사항 목록 조회 (로그인 여부 상관없이)
    @GetMapping("/list")
    public ResponseEntity<List<NoticeVO>> getNoticeList() {
        try {
            List<NoticeVO> noticeList = noticeService.getNoticeList();
            return ResponseEntity.ok(noticeList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // 공지사항 상세 조회 (로그인 여부 상관없이)
    @GetMapping("/{notice_id}")
    public ResponseEntity<NoticeVO> getNoticeDetail(@PathVariable("notice_id") int notice_id) {
        try {
            NoticeVO notice = noticeService.getNoticeDetail(notice_id);
            return ResponseEntity.ok(notice);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
