package org.example.stockfishanalyzer.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PGN 解析工具类
 * 职责：解析 PGN (Portable Game Notation) 棋谱文件
 */
@Slf4j
public class PgnParser {

    // PGN 标签正则表达式：[Event "xxx"]
    private static final Pattern TAG_PATTERN = Pattern.compile("\\[(\\w+)\\s+\"([^\"]*)\"]");

    // 走法正则表达式（简化版）
    private static final Pattern MOVE_PATTERN = Pattern.compile("([NBRQK]?[a-h]?[1-8]?x?[a-h][1-8](?:=[NBRQ])?[+#]?|O-O(?:-O)?)");

    @Data
    public static class PgnGame {
        private Map<String, String> tags = new HashMap<>();
        private List<String> moves = new ArrayList<>();
        private String rawPgn;

        public String getTag(String key) {
            return tags.get(key);
        }
    }

    /**
     * 解析 PGN 字符串
     */
    public static PgnGame parse(String pgnContent) {
        if (pgnContent == null || pgnContent.trim().isEmpty()) {
            throw new IllegalArgumentException("PGN 内容不能为空");
        }

        PgnGame game = new PgnGame();
        game.setRawPgn(pgnContent);

        String[] lines = pgnContent.split("\n");
        StringBuilder moveText = new StringBuilder();

        // 第一步：解析标签
        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("%")) {
                // 忽略空行和注释
                continue;
            }

            if (line.startsWith("[")) {
                // 解析标签
                Matcher matcher = TAG_PATTERN.matcher(line);
                if (matcher.find()) {
                    String key = matcher.group(1);
                    String value = matcher.group(2);
                    game.getTags().put(key, value);
                }
            } else {
                // 走法文本
                moveText.append(line).append(" ");
            }
        }

        // 第二步：解析走法
        String movesString = moveText.toString();
        // 移除走法序号（如 "1.", "2.", 等）
        movesString = movesString.replaceAll("\\d+\\.", "");
        // 移除结果标记（如 "1-0", "0-1", "1/2-1/2", "*"）
        movesString = movesString.replaceAll("(1-0|0-1|1/2-1/2|\\*)", "");
        // 移除注释 {...}
        movesString = movesString.replaceAll("\\{[^}]*\\}", "");
        // 移除变着 (...)
        movesString = movesString.replaceAll("\\([^)]*\\)", "");

        // 提取走法
        Matcher moveMatcher = MOVE_PATTERN.matcher(movesString);
        while (moveMatcher.find()) {
            String move = moveMatcher.group(1);
            game.getMoves().add(move);
        }

        log.debug("解析 PGN 完成 - 标签数: {}, 走法数: {}", game.getTags().size(), game.getMoves().size());

        return game;
    }

    /**
     * 将 SAN 走法转换为 UCI 格式（简化版本）
     * 注意：完整的转换需要维护棋盘状态，这里仅作示例
     */
    public static String sanToUci(String san) {
        // TODO: 实现完整的 SAN 到 UCI 转换
        // 这需要维护棋盘状态，较为复杂
        // 建议使用专业的国际象棋库（如 chess.js 的 Java 移植版）
        log.warn("SAN 到 UCI 转换尚未完全实现: {}", san);
        return san; // 临时返回原值
    }
}
