package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.CreateTagRequest;
import org.example.stockfishanalyzer.dto.TagDTO;
import org.example.stockfishanalyzer.dto.TagWithGamesDTO;
import org.example.stockfishanalyzer.entity.GamePgn;
import org.example.stockfishanalyzer.entity.GameTag;
import org.example.stockfishanalyzer.entity.GameTagRelation;
import org.example.stockfishanalyzer.repository.GamePgnRepository;
import org.example.stockfishanalyzer.repository.GameTagRelationRepository;
import org.example.stockfishanalyzer.repository.GameTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签管理业务逻辑层
 */
@Service
@RequiredArgsConstructor
public class TagService {

    private final GameTagRepository tagRepository;
    private final GameTagRelationRepository relationRepository;
    private final GamePgnRepository gamePgnRepository;

    /**
     * 获取用户所有标签
     */
    public List<TagDTO> getUserTags(Long userId) {
        List<GameTag> tags = tagRepository.findByUserId(userId);
        return tags.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取标签详情（包含关联的棋局列表）
     */
    public TagWithGamesDTO getTagWithGames(Long tagId, Long userId) {
        GameTag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        if (!tag.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该标签");
        }

        // 查询关联的棋局
        List<GameTagRelation> relations = relationRepository.findByTagId(tagId);
        List<TagWithGamesDTO.GameSummary> games = relations.stream()
                .map(rel -> gamePgnRepository.findById(rel.getGameId()))
                .filter(opt -> opt.isPresent())
                .map(opt -> {
                    GamePgn game = opt.get();
                    return new TagWithGamesDTO.GameSummary(
                        game.getId(),
                        game.getWhitePlayer(),
                        game.getBlackPlayer(),
                        game.getGameResult(),
                        game.getGameDate()
                    );
                })
                .collect(Collectors.toList());

        TagWithGamesDTO dto = new TagWithGamesDTO();
        dto.setId(tag.getId());
        dto.setTagName(tag.getTagName());
        dto.setTagColor(tag.getTagColor());
        dto.setGameCount(games.size());
        dto.setGames(games);
        
        return dto;
    }

    /**
     * 创建标签
     */
    @Transactional
    public TagDTO createTag(CreateTagRequest request, Long userId) {
        // 检查标签名是否已存在
        tagRepository.findByUserIdAndTagName(userId, request.getTagName())
                .ifPresent(tag -> {
                    throw new RuntimeException("标签名已存在");
                });

        GameTag tag = new GameTag();
        tag.setUserId(userId);
        tag.setTagName(request.getTagName());
        tag.setTagColor(request.getTagColor() != null ? request.getTagColor() : "#667eea");
        
        GameTag saved = tagRepository.save(tag);
        return convertToDTO(saved);
    }

    /**
     * 更新标签
     */
    @Transactional
    public TagDTO updateTag(Long tagId, CreateTagRequest request, Long userId) {
        GameTag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        if (!tag.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该标签");
        }

        // 如果修改了名称，检查是否与其他标签重名
        if (!tag.getTagName().equals(request.getTagName())) {
            tagRepository.findByUserIdAndTagName(userId, request.getTagName())
                    .ifPresent(existingTag -> {
                        if (!existingTag.getId().equals(tagId)) {
                            throw new RuntimeException("标签名已存在");
                        }
                    });
        }

        tag.setTagName(request.getTagName());
        if (request.getTagColor() != null) {
            tag.setTagColor(request.getTagColor());
        }
        
        GameTag updated = tagRepository.save(tag);
        return convertToDTO(updated);
    }

    /**
     * 删除标签（同时删除所有关联关系）
     */
    @Transactional
    public void deleteTag(Long tagId, Long userId) {
        GameTag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        if (!tag.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该标签");
        }

        // 删除所有关联关系
        List<GameTagRelation> relations = relationRepository.findByTagId(tagId);
        relationRepository.deleteAll(relations);
        
        // 删除标签
        tagRepository.delete(tag);
    }

    /**
     * 为棋局添加标签
     */
    @Transactional
    public void addTagToGame(Long gameId, Long tagId, Long userId) {
        // 验证棋局和标签都存在且属于该用户
        GamePgn game = gamePgnRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("棋局不存在"));
        
        if (!game.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该棋局");
        }

        GameTag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        
        if (!tag.getUserId().equals(userId)) {
            throw new RuntimeException("无权使用该标签");
        }

        // 检查是否已经关联
        List<GameTagRelation> existing = relationRepository.findByGameId(gameId);
        boolean alreadyTagged = existing.stream()
                .anyMatch(rel -> rel.getTagId().equals(tagId));
        
        if (alreadyTagged) {
            throw new RuntimeException("该棋局已有此标签");
        }

        // 创建关联
        GameTagRelation relation = new GameTagRelation();
        relation.setGameId(gameId);
        relation.setTagId(tagId);
        relationRepository.save(relation);
    }

    /**
     * 从棋局移除标签
     */
    @Transactional
    public void removeTagFromGame(Long gameId, Long tagId, Long userId) {
        // 验证权限
        GamePgn game = gamePgnRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("棋局不存在"));
        
        if (!game.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该棋局");
        }

        // 查找并删除关联
        List<GameTagRelation> relations = relationRepository.findByGameId(gameId);
        relations.stream()
                .filter(rel -> rel.getTagId().equals(tagId))
                .findFirst()
                .ifPresent(relationRepository::delete);
    }

    /**
     * 获取棋局的所有标签
     */
    public List<TagDTO> getGameTags(Long gameId, Long userId) {
        // 验证权限
        GamePgn game = gamePgnRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("棋局不存在"));
        
        if (!game.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该棋局");
        }

        List<GameTagRelation> relations = relationRepository.findByGameId(gameId);
        List<TagDTO> tags = new ArrayList<>();
        
        for (GameTagRelation relation : relations) {
            tagRepository.findById(relation.getTagId()).ifPresent(tag -> {
                tags.add(convertToDTO(tag));
            });
        }
        
        return tags;
    }

    /**
     * 转换为 DTO
     */
    private TagDTO convertToDTO(GameTag tag) {
        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setUserId(tag.getUserId());
        dto.setTagName(tag.getTagName());
        dto.setTagColor(tag.getTagColor());
        dto.setCreatedAt(tag.getCreatedAt());
        
        // 统计使用该标签的棋局数
        List<GameTagRelation> relations = relationRepository.findByTagId(tag.getId());
        dto.setGameCount(relations.size());
        
        return dto;
    }
}
