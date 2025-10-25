package org.example.stockfishanalyzer.controller;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.BookmarkDTO;
import org.example.stockfishanalyzer.dto.CreateBookmarkRequest;
import org.example.stockfishanalyzer.service.BookmarkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 书签管理控制器
 */
@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * 获取用户所有书签
     * GET /api/bookmarks?userId=1
     */
    @GetMapping
    public ResponseEntity<List<BookmarkDTO>> getUserBookmarks(@RequestParam Long userId) {
        List<BookmarkDTO> bookmarks = bookmarkService.getUserBookmarks(userId);
        return ResponseEntity.ok(bookmarks);
    }

    /**
     * 获取指定棋局的书签
     * GET /api/bookmarks/game/{gameId}?userId=1
     */
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<BookmarkDTO>> getBookmarksByGame(
            @PathVariable Long gameId,
            @RequestParam Long userId) {
        List<BookmarkDTO> bookmarks = bookmarkService.getBookmarksByGame(userId, gameId);
        return ResponseEntity.ok(bookmarks);
    }

    /**
     * 获取单个书签详情
     * GET /api/bookmarks/{id}?userId=1
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookmarkDTO> getBookmarkById(
            @PathVariable Long id,
            @RequestParam Long userId) {
        BookmarkDTO bookmark = bookmarkService.getBookmarkById(id, userId);
        return ResponseEntity.ok(bookmark);
    }

    /**
     * 创建书签
     * POST /api/bookmarks?userId=1
     * Body: { "gameId": 1, "moveNumber": 15, "fenPosition": "...", "note": "..." }
     */
    @PostMapping
    public ResponseEntity<BookmarkDTO> createBookmark(
            @RequestBody CreateBookmarkRequest request,
            @RequestParam Long userId) {
        BookmarkDTO created = bookmarkService.createBookmark(request, userId);
        return ResponseEntity.ok(created);
    }

    /**
     * 更新书签备注
     * PUT /api/bookmarks/{id}?userId=1
     * Body: { "note": "..." }
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookmarkDTO> updateBookmark(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestParam Long userId) {
        String note = request.get("note");
        BookmarkDTO updated = bookmarkService.updateBookmark(id, note, userId);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除书签
     * DELETE /api/bookmarks/{id}?userId=1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBookmark(
            @PathVariable Long id,
            @RequestParam Long userId) {
        bookmarkService.deleteBookmark(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "书签已删除");
        return ResponseEntity.ok(response);
    }
}
