package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.BookmarkDTO;
import org.example.stockfishanalyzer.dto.CreateBookmarkRequest;
import org.example.stockfishanalyzer.entity.GamePgn;
import org.example.stockfishanalyzer.entity.PositionBookmark;
import org.example.stockfishanalyzer.repository.GamePgnRepository;
import org.example.stockfishanalyzer.repository.PositionBookmarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 书签业务逻辑层
 */
@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final PositionBookmarkRepository bookmarkRepository;
    private final GamePgnRepository gamePgnRepository;

    /**
     * 获取用户所有书签
     */
    public List<BookmarkDTO> getUserBookmarks(Long userId) {
        List<PositionBookmark> bookmarks = bookmarkRepository.findByUserId(userId);
        return bookmarks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定棋局的书签
     */
    public List<BookmarkDTO> getBookmarksByGame(Long userId, Long gameId) {
        List<PositionBookmark> bookmarks = bookmarkRepository.findByUserIdAndGameId(userId, gameId);
        return bookmarks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取单个书签详情
     */
    public BookmarkDTO getBookmarkById(Long bookmarkId, Long userId) {
        PositionBookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("书签不存在"));
        
        if (!bookmark.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该书签");
        }
        
        return convertToDTO(bookmark);
    }

    /**
     * 创建书签
     */
    @Transactional
    public BookmarkDTO createBookmark(CreateBookmarkRequest request, Long userId) {
        // 验证棋局是否存在
        GamePgn game = gamePgnRepository.findById(request.getGameId())
                .orElseThrow(() -> new RuntimeException("棋局不存在"));
        
        // 验证用户权限
        if (!game.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该棋局");
        }
        
        PositionBookmark bookmark = new PositionBookmark();
        bookmark.setGameId(request.getGameId());
        bookmark.setUserId(userId);
        bookmark.setMoveNumber(request.getMoveNumber());
        bookmark.setFenPosition(request.getFenPosition());
        bookmark.setNote(request.getNote());
        
        PositionBookmark saved = bookmarkRepository.save(bookmark);
        return convertToDTO(saved);
    }

    /**
     * 更新书签备注
     */
    @Transactional
    public BookmarkDTO updateBookmark(Long bookmarkId, String note, Long userId) {
        PositionBookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("书签不存在"));
        
        if (!bookmark.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该书签");
        }
        
        bookmark.setNote(note);
        PositionBookmark updated = bookmarkRepository.save(bookmark);
        return convertToDTO(updated);
    }

    /**
     * 删除书签
     */
    @Transactional
    public void deleteBookmark(Long bookmarkId, Long userId) {
        PositionBookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("书签不存在"));
        
        if (!bookmark.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该书签");
        }
        
        bookmarkRepository.delete(bookmark);
    }

    /**
     * 转换为 DTO
     */
    private BookmarkDTO convertToDTO(PositionBookmark bookmark) {
        BookmarkDTO dto = new BookmarkDTO();
        dto.setId(bookmark.getId());
        dto.setGameId(bookmark.getGameId());
        dto.setUserId(bookmark.getUserId());
        dto.setMoveNumber(bookmark.getMoveNumber());
        dto.setFenPosition(bookmark.getFenPosition());
        dto.setNote(bookmark.getNote());
        dto.setCreatedAt(bookmark.getCreatedAt());
        
        // 查询关联的棋局信息
        gamePgnRepository.findById(bookmark.getGameId()).ifPresent(game -> {
            // 构建棋局标题
            String title = game.getWhitePlayer() + " vs " + game.getBlackPlayer();
            if (game.getGameDate() != null && !game.getGameDate().equals("????.??.??")) {
                title = game.getGameDate() + " - " + title;
            }
            dto.setGameTitle(title);
        });
        
        return dto;
    }
}
