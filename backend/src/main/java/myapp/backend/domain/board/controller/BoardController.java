package myapp.backend.domain.board.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import myapp.backend.domain.board.service.BoardService;
import myapp.backend.domain.board.vo.BoardVO;
import myapp.backend.domain.auth.vo.UserPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("api/board")
public class BoardController {
    @Autowired
    private BoardService boardService;
    
    @GetMapping("/board")
    public ResponseEntity<List<BoardVO>> getBoardList() {
        System.out.println("[BoardController] getBoardList API 호출됨");
        
        try {
            System.out.println("[BoardController] boardService.getBoardList() 호출 시작");
            List<BoardVO> boardList = boardService.getBoardList();
            System.out.println("[BoardController] boardService.getBoardList() 완료, 결과: " + boardList.size() + "개");
            return ResponseEntity.ok(boardList);
        } catch (Exception e) {
            System.out.println("[BoardController] getBoardList 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 인증된 사용자를 위한 게시글 목록 조회 (좋아요 상태 포함)
    @GetMapping("/board/authenticated")
    public ResponseEntity<List<BoardVO>> getBoardListWithLikeStatus(@AuthenticationPrincipal UserPrincipal principal) {
        System.out.println("[BoardController] getBoardListWithLikeStatus API 호출되었습니다");
        
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        
        try {
            System.out.println("[BoardController] boardService.getBoardListWithLikeStatus() 호출 시작 - userId: " + principal.getUserId());
            List<BoardVO> boardList = boardService.getBoardListWithLikeStatus(principal.getUserId());
            System.out.println("[BoardController] boardService.getBoardListWithLikeStatus() 완료, 결과: " + boardList.size() + "개");
            return ResponseEntity.ok(boardList);
        } catch (Exception e) {
            System.out.println("[BoardController] getBoardListWithLikeStatus 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 게시물 작성 (제목 + 내용 + 이미지 첨부) - 인증된 사용자만
    @PostMapping("board/insert")
    public ResponseEntity<?> createBoard(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        // 인증된 사용자만 게시글 작성 가능
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        
        try {
            int userId = principal.getUserId();
            boardService.createBoardWithTitleAndImages(title, content, images, userId);
            return ResponseEntity.ok().body("게시글이 성공적으로 작성되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 작성에 실패했습니다: " + e.getMessage());
        }
    }
    
    
    // 개별 글 조회
    @GetMapping("/board/{board_id}")
    public BoardVO getBoard(@PathVariable("board_id") int board_id) {
        return boardService.getBoard(board_id);
    }
    
    // 조회수 증가 (별도 API)
    @PostMapping("board/{board_id}/view")
    public void increaseViewCount(@PathVariable("board_id") int board_id) {
        boardService.increaseViewCount(board_id);
    }

    // 게시물 상세페이지 (디테일)
    @GetMapping("/board/detail/{board_id}")
    public BoardVO getBoardDetail(
            @PathVariable("board_id") int board_id,
            @RequestParam(value = "currentUserId", required = false) Integer currentUserId) {
        return boardService.getBoardDetail(board_id, currentUserId);
    }

    // 게시물 삭제 (작성자만)
    @DeleteMapping("/board/delete/{board_id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable("board_id") int board_id,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        boardService.deleteBoard(board_id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    // 게시물 수정 (작성자만)
    @PutMapping("/update/{board_id}")
    public ResponseEntity<Void> updateBoard(@PathVariable("board_id") int board_id,
                                            @RequestBody BoardVO updatedBoard,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            System.out.println("AuthenticationPrincipal is null -> 401 반환");
            return ResponseEntity.status(401).build();
        }
        // PathVariable을 우선시하여 보안 강화
        updatedBoard.setBoard_id(board_id);
        boardService.updateBoard(board_id, updatedBoard, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
    
    // 게시물 수정 (제목 + 내용 + 이미지 포함) - 작성자만
    @PutMapping("/update-with-images/{board_id}")
    public ResponseEntity<?> updateBoardWithImages(
            @PathVariable("board_id") int board_id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        
        try {
            boardService.updateBoardWithImages(board_id, title, content, images, principal.getUserId());
            return ResponseEntity.ok().body("게시글이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 수정에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 이미지 조회 API (인증 없이 접근 가능)
    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable("filename") String filename) {
        try {
            // 이미지 파일 경로 설정 (EB 환경 고려)
            String userDir = System.getProperty("user.dir");
            String[] possiblePaths;
            
            if (userDir.contains("/var/app/current") || userDir.contains("/var/app")) {
                // EB 환경 경로
                possiblePaths = new String[]{
                    userDir + "/src/main/resources/static/upload/",
                    userDir + "/build/resources/main/static/upload/",
                    userDir + "/static/upload/"
                };
            } else {
                // 로컬 개발 환경 경로
                possiblePaths = new String[]{
                    userDir + "/src/main/resources/static/upload/",
                    userDir + "/build/resources/main/static/upload/",
                    userDir + "/backend/src/main/resources/static/upload/",
                    userDir + "/backend/build/resources/main/static/upload/",
                "src/main/resources/static/upload/",
                    "build/resources/main/static/upload/"
            };
            }
            
            Path filePath = null;
            String usedPath = null;
            
            // 여러 경로에서 파일 찾기
            System.out.println("[BoardController] 이미지 파일 검색 시작: " + filename);
            for (String path : possiblePaths) {
                Path testPath = Paths.get(path + filename);
                boolean exists = testPath.toFile().exists();
                System.out.println("  - 경로: " + path + filename + " (존재: " + exists + ")");
                if (exists) {
                    filePath = testPath;
                    usedPath = path;
                    System.out.println("  - 파일 발견: " + path);
                    break;
                }
            }
            
            if (filePath == null) {
                System.err.println("[BoardController] 이미지 파일을 찾을 수 없음: " + filename);
                System.err.println("[BoardController] 시도한 경로들:");
                for (String path : possiblePaths) {
                    System.err.println("  - " + path + filename + " (존재: " + Paths.get(path + filename).toFile().exists() + ")");
                }
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("[BoardController] 이미지 파일 발견: " + usedPath + filename);
            Resource resource = new FileSystemResource(filePath.toFile());
        
            if (resource.exists() && resource.isReadable()) {
                // 이미지 타입 설정
                String contentType = determineContentType(filename);
                
                System.out.println("[BoardController] 이미지 응답 성공:");
                System.out.println("  - 파일명: " + filename);
                System.out.println("  - 경로: " + filePath.toAbsolutePath());
                System.out.println("  - 크기: " + resource.contentLength() + " bytes");
                System.out.println("  - Content-Type: " + contentType);
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
            } else {
                System.err.println("[BoardController] 이미지 리소스 문제:");
                System.err.println("  - 파일명: " + filename);
                System.err.println("  - 경로: " + filePath.toAbsolutePath());
                System.err.println("  - 존재: " + resource.exists());
                System.err.println("  - 읽기 가능: " + resource.isReadable());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 파일 확장자에 따른 Content-Type 결정
    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
    
    // 좋아요 토글 (추가/취소) - 인증된 사용자만
    @PostMapping("/board/{board_id}/like")
    public ResponseEntity<?> toggleBoardLike(
            @PathVariable("board_id") int board_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        
        try {
            boolean isLiked = boardService.toggleBoardLike(board_id, principal.getUserId());
            String message = isLiked ? "좋아요가 추가되었습니다." : "좋아요가 취소되었습니다.";
            return ResponseEntity.ok().body(Map.of(
                "message", message,
                "isLiked", isLiked
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "좋아요 처리에 실패했습니다: " + e.getMessage()
            ));
        }
    }
    
    // 게시글 좋아요 상태 확인 - 인증된 사용자만
    @GetMapping("/board/{board_id}/like-status")
    public ResponseEntity<?> getBoardLikeStatus(
            @PathVariable("board_id") int board_id,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        
        try {
            boolean isLiked = boardService.isLikedByUser(board_id, principal.getUserId());
            int likeCount = boardService.getBoardLikeCount(board_id);
            
            return ResponseEntity.ok().body(Map.of(
                "isLiked", isLiked,
                "likeCount", likeCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("좋아요 상태 확인에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 게시글 좋아요 수 조회 - 인증 없이 접근 가능
    @GetMapping("/board/{board_id}/like-count")
    public ResponseEntity<?> getBoardLikeCount(@PathVariable("board_id") int board_id) {
        try {
            int likeCount = boardService.getBoardLikeCount(board_id);
            return ResponseEntity.ok().body(Map.of("likeCount", likeCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("좋아요 수 조회에 실패했습니다: " + e.getMessage());
        }
    }
}