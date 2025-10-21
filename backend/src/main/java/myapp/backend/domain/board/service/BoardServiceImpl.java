package myapp.backend.domain.board.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import myapp.backend.domain.board.mapper.BoardMapper;
import myapp.backend.domain.board.vo.BoardVO;
import myapp.backend.domain.board.vo.ImageVO;
import myapp.backend.domain.board.vo.BoardLikeVO;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class BoardServiceImpl implements BoardService {
    @Autowired
    private BoardMapper boardMapper;
    
    @Override
    public List<BoardVO> getBoardList() {
        List<BoardVO> boardList = boardMapper.getBoardList();
        
        // 각 게시글에 대해 imageUrls 설정
        for (BoardVO board : boardList) {
            if (board.getImage_url() != null && board.getImage_url().contains(",")) {
                String[] imageUrlArray = board.getImage_url().split(",");
                List<String> imageUrls = new ArrayList<>();
                for (String url : imageUrlArray) {
                    imageUrls.add(url.trim());
                }
                board.setImageUrls(imageUrls);
            } else if (board.getImage_url() != null) {
                // 단일 이미지인 경우
                List<String> imageUrls = new ArrayList<>();
                imageUrls.add(board.getImage_url());
                board.setImageUrls(imageUrls);
            }
        }
        
        return boardList;
    }
    
    @Override
    public void createBoard(BoardVO board) {
        boardMapper.insertBoard(board); // 게시물 작성
    }
    
    @Override
    public void createBoardWithImages(String content, MultipartFile[] images, int userId) {
        try {
            // 1. 게시글 먼저 저장
            BoardVO board = new BoardVO();
            board.setBoard_title(""); // 제목 없이 내용만 저장 (기존 방식 유지)
            board.setBoard_content(content);
            board.setUser_id(userId);
            boardMapper.insertBoard(board);
            
            // 2. board_id가 설정되었는지 확인
            if (board.getBoard_id() == 0) {
                // board_id가 설정되지 않았다면 최근 생성된 게시글 조회
                board = boardMapper.getLatestBoardByUserId(userId);
            }
            
            // 3. 이미지가 있으면 처리 (여러 이미지를 쉼표로 구분해서 저장)
            if (images != null && images.length > 0) {
                StringBuilder allImageUrls = new StringBuilder();
                
                for (int i = 0; i < images.length; i++) {
                    MultipartFile image = images[i];
                    if (!image.isEmpty()) {
                        // 이미지 저장
                        ImageSaveResult result = saveImageAndConnectToBoard(image, board.getBoard_id(), i);
                        
                        // 모든 이미지 URL을 쉼표로 구분해서 저장
                        if (i > 0) {
                            allImageUrls.append(",");
                        }
                        allImageUrls.append(result.getImageUrl());
                    }
                }
                
                // 4. 모든 이미지 URL을 하나의 ImageVO에 저장
                ImageVO combinedImageVO = new ImageVO();
                combinedImageVO.setImage_url(allImageUrls.toString());
                combinedImageVO.setImage_id(0); // MyBatis가 자동으로 설정
                
                boardMapper.insertImage(combinedImageVO);
                
                // 5. 게시글의 image_id 업데이트
                boardMapper.updateBoardImageId(board.getBoard_id(), combinedImageVO.getImage_id());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("게시글 작성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public void createBoardWithTitleAndImages(String title, String content, MultipartFile[] images, int userId) {
        try {
            // 1. 게시글 먼저 저장 (제목과 내용을 분리해서 저장)
            BoardVO board = new BoardVO();
            board.setBoard_title(title);
            board.setBoard_content(content);
            board.setUser_id(userId);
            boardMapper.insertBoard(board);
            
            // 2. board_id가 설정되었는지 확인
            if (board.getBoard_id() == 0) {
                // board_id가 설정되지 않았다면 최근 생성된 게시글 조회
                board = boardMapper.getLatestBoardByUserId(userId);
            }
            
            // 3. 이미지가 있으면 처리 (여러 이미지를 쉼표로 구분해서 저장)
            if (images != null && images.length > 0) {
                StringBuilder allImageUrls = new StringBuilder();
                
                for (int i = 0; i < images.length; i++) {
                    MultipartFile image = images[i];
                    if (!image.isEmpty()) {
                        // 이미지 저장
                        ImageSaveResult result = saveImageAndConnectToBoard(image, board.getBoard_id(), i);
                        
                        // 모든 이미지 URL을 쉼표로 구분해서 저장
                        if (i > 0) {
                            allImageUrls.append(",");
                        }
                        allImageUrls.append(result.getImageUrl());
                    }
                }
                
                // 4. 모든 이미지 URL을 하나의 ImageVO에 저장
                ImageVO combinedImageVO = new ImageVO();
                combinedImageVO.setImage_url(allImageUrls.toString());
                combinedImageVO.setImage_id(0); // MyBatis가 자동으로 설정
                
                boardMapper.insertImage(combinedImageVO);
                
                // 5. 게시글의 image_id 업데이트
                boardMapper.updateBoardImageId(board.getBoard_id(), combinedImageVO.getImage_id());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("게시글 작성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 이미지 저장 및 게시글 연결
    private ImageSaveResult saveImageAndConnectToBoard(MultipartFile image, int boardId, int order) throws IOException {
        // 업로드 디렉토리 설정
        String uploadDir = System.getProperty("user.dir") + "/backend/src/main/resources/static/upload/";
        
        // 디렉토리가 없으면 생성
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }
        
        // 파일명 생성 (UUID + 원본 확장자)
        String originalFilename = image.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID().toString() + extension;
        
        // 파일 저장
        Path filePath = Paths.get(uploadDir, storedFilename);
        Files.copy(image.getInputStream(), filePath);
        
        // 이미지 URL 생성
        String imageUrl = "/upload/" + storedFilename;
        
        // ImageVO 생성 (실제로는 사용하지 않음, 로깅용)
        ImageVO imageVO = new ImageVO();
        imageVO.setImage_url(imageUrl);
        imageVO.setImage_id(0);
        
        // MyBatis keyProperty로 설정된 실제 이미지 ID 사용
        int actualImageId = imageVO.getImage_id();
        
        System.out.println("이미지 저장됨: " + storedFilename + " (게시글 ID: " + boardId + ", 순서: " + order + ")");
        System.out.println("저장 경로: " + filePath.toAbsolutePath());
        System.out.println("이미지 URL: " + imageUrl);
        System.out.println("MyBatis 반환값: " + actualImageId);
        System.out.println("실제 이미지 ID: " + actualImageId);
        
        return new ImageSaveResult(actualImageId, imageUrl);
    }
    
    // 이미지 저장 결과를 담는 내부 클래스
    private static class ImageSaveResult {
        private final int imageId;
        private final String imageUrl;
        
        public ImageSaveResult(int imageId, String imageUrl) {
            this.imageId = imageId;
            this.imageUrl = imageUrl;
        }
        
        public int getImageId() { return imageId; }
        public String getImageUrl() { return imageUrl; }
    }
    
    @Override
    public BoardVO getBoard(int board_id) {
        // 조회수 증가
        increaseViewCount(board_id);
        // 게시글 조회
        return boardMapper.getBoardDetailById(board_id); // 개별 글 조회
    }
    
    @Override
    public void increaseViewCount(int board_id) {
        boardMapper.updateViewCount(board_id);
    }
    
    @Override
    public BoardVO getBoardDetail(int board_id, Integer currentUserId) {
        // 조회수 증가
        increaseViewCount(board_id);
        
        // 상세 정보 조회
        BoardVO boardDetail = boardMapper.getBoardDetailById(board_id);
        
        // 여러 이미지 URL을 배열로 설정
        if (boardDetail != null && boardDetail.getImage_url() != null && boardDetail.getImage_url().contains(",")) {
            String[] imageUrlArray = boardDetail.getImage_url().split(",");
            List<String> imageUrls = new ArrayList<>();
            for (String url : imageUrlArray) {
                imageUrls.add(url.trim());
            }
            boardDetail.setImageUrls(imageUrls);
        } else if (boardDetail != null && boardDetail.getImage_url() != null) {
            // 단일 이미지인 경우
            List<String> imageUrls = new ArrayList<>();
            imageUrls.add(boardDetail.getImage_url());
            boardDetail.setImageUrls(imageUrls);
        }
        
        return boardDetail;
    }

    @Override
    public void deleteBoard(int boardId, int requestUserId) {
        System.out.println("[BoardServiceImpl] deleteBoard 시작 - boardId: " + boardId);
        
        Integer authorUserId = boardMapper.findAuthorUserId(boardId);
        if (authorUserId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        if (!authorUserId.equals(requestUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 삭제할 수 있습니다.");
        }
        
        // 게시글의 이미지 정보 조회
        BoardVO existingBoard = boardMapper.getBoardDetailById(boardId);
        if (existingBoard != null && existingBoard.getImage_id() != 0) {
            System.out.println("[BoardServiceImpl] 이미지 삭제 시작 - imageId: " + existingBoard.getImage_id());
            
            try {
                // 이미지 레코드 삭제
                boardMapper.deleteImage(existingBoard.getImage_id());
                System.out.println("[BoardServiceImpl] 이미지 레코드 삭제 완료");
                
                // 실제 이미지 파일 삭제
                if (existingBoard.getImage_url() != null) {
                    deleteImageFiles(existingBoard.getImage_url());
                    System.out.println("[BoardServiceImpl] 이미지 파일 삭제 완료");
                }
                
            } catch (Exception e) {
                System.err.println("[BoardServiceImpl] 이미지 삭제 중 오류: " + e.getMessage());
                // 이미지 삭제 실패해도 게시글은 삭제 진행
            }
        }
        
        // 게시글 삭제
        int deleted = boardMapper.deleteBoard(boardId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제 대상이 존재하지 않습니다.");
        }
        
        System.out.println("[BoardServiceImpl] deleteBoard 완료 - boardId: " + boardId);
    }

    @Override
    public void updateBoard(int boardId, BoardVO updatedBoard, int requestUserId) {
        Integer authorUserId = boardMapper.findAuthorUserId(boardId);
        if (authorUserId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        if (!authorUserId.equals(requestUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 수정할 수 있습니다.");
        }

        // 업데이트 대상 보드 ID를 확실히 설정
        updatedBoard.setBoard_id(boardId);
        int updated = boardMapper.updateBoard(updatedBoard);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "수정 대상이 존재하지 않습니다.");
        }
    }
    
    @Override
    public void updateBoardWithImages(int boardId, String title, String content, MultipartFile[] images, int requestUserId) {
        // 1. 권한 확인
        Integer authorUserId = boardMapper.findAuthorUserId(boardId);
        if (authorUserId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        if (!authorUserId.equals(requestUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 수정할 수 있습니다.");
        }
        
        try {
            // 2. 기존 게시글 정보 조회
            BoardVO existingBoard = boardMapper.getBoardDetailById(boardId);
            if (existingBoard == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "수정할 게시글을 찾을 수 없습니다.");
            }
            
            // 3. 게시글 내용 업데이트 (제목과 내용을 분리해서 저장)
            BoardVO updateBoard = new BoardVO();
            updateBoard.setBoard_id(boardId);
            updateBoard.setBoard_title(title);
            updateBoard.setBoard_content(content);
            boardMapper.updateBoard(updateBoard);
            
            // 5. 이미지가 있으면 기존 이미지 삭제 후 새 이미지 업로드
            if (images != null && images.length > 0) {
                // 기존 이미지 정보 삭제
                if (existingBoard.getImage_id() != 0) {
                    // 기존 이미지 레코드 삭제
                    boardMapper.deleteImage(existingBoard.getImage_id());
                    
                    // 기존 이미지 파일들 삭제
                    if (existingBoard.getImage_url() != null) {
                        deleteImageFiles(existingBoard.getImage_url());
                    }
                }
                
                // 새 이미지 업로드 및 저장
                StringBuilder allImageUrls = new StringBuilder();
                
                for (int i = 0; i < images.length; i++) {
                    MultipartFile image = images[i];
                    if (!image.isEmpty()) {
                        // 이미지 저장
                        ImageSaveResult result = saveImageAndConnectToBoard(image, boardId, i);
                        
                        // 모든 이미지 URL을 쉼표로 구분해서 저장
                        if (i > 0) {
                            allImageUrls.append(",");
                        }
                        allImageUrls.append(result.getImageUrl());
                    }
                }
                
                // 새 이미지 정보 저장
                ImageVO newImageVO = new ImageVO();
                newImageVO.setImage_url(allImageUrls.toString());
                newImageVO.setImage_id(0);
                
                boardMapper.insertImage(newImageVO);
                
                // 게시글의 image_id 업데이트
                boardMapper.updateBoardImageId(boardId, newImageVO.getImage_id());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("게시글 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 좋아요 토글 (추가/취소)
    @Override
    public boolean toggleBoardLike(int board_id, int userId) {
        try {
            // 현재 좋아요 상태 확인
            boolean isLiked = boardMapper.existsBoardLike(userId, board_id);
            
            if (isLiked) {
                // 이미 좋아요한 상태면 취소
                boardMapper.deleteBoardLike(userId, board_id);
                System.out.println("[BoardServiceImpl] 좋아요 취소 - boardId: " + board_id + ", userId: " + userId);
                return false; // 좋아요 취소됨
            } else {
                // 좋아요하지 않은 상태면 추가
                BoardLikeVO boardLike = new BoardLikeVO(userId, board_id);
                boardMapper.insertBoardLike(boardLike);
                System.out.println("[BoardServiceImpl] 좋아요 추가 - boardId: " + board_id + ", userId: " + userId);
                return true; // 좋아요 추가됨
            }
        } catch (Exception e) {
            System.err.println("[BoardServiceImpl] 좋아요 토글 중 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("좋아요 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 사용자가 해당 게시글을 좋아요했는지 확인
    @Override
    public boolean isLikedByUser(int board_id, int userId) {
        return boardMapper.existsBoardLike(userId, board_id);
    }
    
    // 게시글의 좋아요 수 조회
    @Override
    public int getBoardLikeCount(int board_id) {
        return boardMapper.getBoardLikeCount(board_id);
    }
    
    // 인증된 사용자를 위한 게시글 목록 조회 (좋아요 상태 포함)
    @Override
    public List<BoardVO> getBoardListWithLikeStatus(int userId) {
        List<BoardVO> boardList = boardMapper.getBoardListWithLikeStatus(userId);
        
        // 각 게시글에 대해 imageUrls 설정
        for (BoardVO board : boardList) {
            if (board.getImage_url() != null && board.getImage_url().contains(",")) {
                String[] imageUrlArray = board.getImage_url().split(",");
                List<String> imageUrls = new ArrayList<>();
                for (String url : imageUrlArray) {
                    imageUrls.add(url.trim());
                }
                board.setImageUrls(imageUrls);
            } else if (board.getImage_url() != null) {
                // 단일 이미지인 경우
                List<String> imageUrls = new ArrayList<>();
                imageUrls.add(board.getImage_url());
                board.setImageUrls(imageUrls);
            }
        }
        
        return boardList;
    }

    
    // 이미지 파일들을 실제로 삭제하는 메서드
    private void deleteImageFiles(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }
        
        // 쉼표로 구분된 여러 이미지 URL 처리
        String[] imageUrls = imageUrl.split(",");
        
        for (String url : imageUrls) {
            String filename = url.trim();
            if (filename.startsWith("/upload/")) {
                filename = filename.substring(8); // "/upload/" 제거
            }
            
            // 여러 경로에서 파일 찾아서 삭제
            String[] possiblePaths = {
                System.getProperty("user.dir") + "/backend/src/main/resources/static/upload/",
                System.getProperty("user.dir") + "/backend/build/resources/main/static/upload/",
                System.getProperty("user.dir") + "/src/main/resources/static/upload/",
                System.getProperty("user.dir") + "/build/resources/main/static/upload/"
            };
            
            for (String path : possiblePaths) {
                java.io.File file = new java.io.File(path + filename);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    System.out.println("[BoardServiceImpl] 이미지 파일 삭제: " + path + filename + " (성공: " + deleted + ")");
                    break; // 한 경로에서 삭제되면 다음 경로는 시도하지 않음
                }
            }
        }
    }
}