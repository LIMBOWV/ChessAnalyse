package org.example.stockfishanalyzer.controller;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.CreateTagRequest;
import org.example.stockfishanalyzer.dto.TagDTO;
import org.example.stockfishanalyzer.dto.TagWithGamesDTO;
import org.example.stockfishanalyzer.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签管理控制器
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 获取用户所有标签
     * GET /api/tags?userId=1
     */
    @GetMapping
    public ResponseEntity<List<TagDTO>> getUserTags(@RequestParam Long userId) {
        List<TagDTO> tags = tagService.getUserTags(userId);
        return ResponseEntity.ok(tags);
    }

    /**
     * 获取标签详情（包含关联的棋局）
     * GET /api/tags/{id}?userId=1
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagWithGamesDTO> getTagWithGames(
            @PathVariable Long id,
            @RequestParam Long userId) {
        TagWithGamesDTO tag = tagService.getTagWithGames(id, userId);
        return ResponseEntity.ok(tag);
    }

    /**
     * 创建标签
     * POST /api/tags?userId=1
     * Body: { "tagName": "重要比赛", "tagColor": "#ff5722" }
     */
    @PostMapping
    public ResponseEntity<TagDTO> createTag(
            @RequestBody CreateTagRequest request,
            @RequestParam Long userId) {
        TagDTO created = tagService.createTag(request, userId);
        return ResponseEntity.ok(created);
    }

    /**
     * 更新标签
     * PUT /api/tags/{id}?userId=1
     * Body: { "tagName": "重要比赛", "tagColor": "#ff5722" }
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> updateTag(
            @PathVariable Long id,
            @RequestBody CreateTagRequest request,
            @RequestParam Long userId) {
        TagDTO updated = tagService.updateTag(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除标签
     * DELETE /api/tags/{id}?userId=1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTag(
            @PathVariable Long id,
            @RequestParam Long userId) {
        tagService.deleteTag(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "标签已删除");
        return ResponseEntity.ok(response);
    }

    /**
     * 为棋局添加标签
     * POST /api/tags/{tagId}/games/{gameId}?userId=1
     */
    @PostMapping("/{tagId}/games/{gameId}")
    public ResponseEntity<Map<String, String>> addTagToGame(
            @PathVariable Long tagId,
            @PathVariable Long gameId,
            @RequestParam Long userId) {
        tagService.addTagToGame(gameId, tagId, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "标签已添加到棋局");
        return ResponseEntity.ok(response);
    }

    /**
     * 从棋局移除标签
     * DELETE /api/tags/{tagId}/games/{gameId}?userId=1
     */
    @DeleteMapping("/{tagId}/games/{gameId}")
    public ResponseEntity<Map<String, String>> removeTagFromGame(
            @PathVariable Long tagId,
            @PathVariable Long gameId,
            @RequestParam Long userId) {
        tagService.removeTagFromGame(gameId, tagId, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "标签已从棋局移除");
        return ResponseEntity.ok(response);
    }

    /**
     * 获取棋局的所有标签
     * GET /api/tags/game/{gameId}?userId=1
     */
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<TagDTO>> getGameTags(
            @PathVariable Long gameId,
            @RequestParam Long userId) {
        List<TagDTO> tags = tagService.getGameTags(gameId, userId);
        return ResponseEntity.ok(tags);
    }
}
