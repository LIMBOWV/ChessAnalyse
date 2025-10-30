package org.example.stockfishanalyzer.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 简化版国际象棋引擎
 * 职责：维护棋盘状态，将 SAN 走法转换为 UCI 格式
 */
@Slf4j
public class SimpleChessEngine {

    private char[][] board;
    private boolean whiteTurn;
    // TODO: 未来版本支持王车易位
    // private Set<String> castlingRights;
    // TODO: 未来版本支持吃过路兵
    // private String enPassantSquare;

    public SimpleChessEngine() {
        initBoard();
    }

    /**
     * 初始化棋盘为标准开局位置
     */
    private void initBoard() {
        board = new char[8][8];
        whiteTurn = true;
        // castlingRights = new HashSet<>(Arrays.asList("K", "Q", "k", "q"));
        // enPassantSquare = "-";

        // 初始化棋盘
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = '.';
            }
        }

        // 白方棋子
        board[7][0] = 'R'; board[7][7] = 'R';
        board[7][1] = 'N'; board[7][6] = 'N';
        board[7][2] = 'B'; board[7][5] = 'B';
        board[7][3] = 'Q';
        board[7][4] = 'K';
        for (int i = 0; i < 8; i++) {
            board[6][i] = 'P';
        }

        // 黑方棋子
        board[0][0] = 'r'; board[0][7] = 'r';
        board[0][1] = 'n'; board[0][6] = 'n';
        board[0][2] = 'b'; board[0][5] = 'b';
        board[0][3] = 'q';
        board[0][4] = 'k';
        for (int i = 0; i < 8; i++) {
            board[1][i] = 'p';
        }
    }

    /**
     * 将 SAN 走法转换为 UCI 格式
     * 注意：这是一个简化实现，可能不支持所有复杂情况
     */
    public String sanToUci(String san) {
        try {
            san = san.replace("+", "").replace("#", "").replace("!", "").replace("?", "").trim();

            // 王车易位
            if ("O-O".equals(san) || "0-0".equals(san)) {
                return whiteTurn ? "e1g1" : "e8g8";
            }
            if ("O-O-O".equals(san) || "0-0-0".equals(san)) {
                return whiteTurn ? "e1c1" : "e8c8";
            }

            // 解析目标格子
            String targetSquare = extractTargetSquare(san);
            if (targetSquare == null) {
                log.warn("无法解析目标格子: {}", san);
                return san; // 返回原值
            }

            // 解析棋子类型
            char piece = extractPiece(san);
            if (!whiteTurn && piece >= 'A' && piece <= 'Z') {
                piece = Character.toLowerCase(piece);
            }

            // 查找起始格子
            String fromSquare = findFromSquare(piece, san, targetSquare);
            if (fromSquare == null) {
                log.warn("无法找到起始格子: {}", san);
                return san;
            }

            // 升变处理
            String promotion = "";
            if (san.contains("=")) {
                int idx = san.indexOf('=');
                if (idx + 1 < san.length()) {
                    char promotionPiece = san.charAt(idx + 1);
                    promotion = String.valueOf(Character.toLowerCase(promotionPiece));
                }
            }

            return fromSquare + targetSquare + promotion;

        } catch (Exception e) {
            log.error("SAN 到 UCI 转换失败: {}", san, e);
            return san;
        }
    }

    /**
     * 执行 UCI 走法
     */
    public void makeUciMove(String uci) {
        if (uci.length() < 4) return;

        int fromFile = uci.charAt(0) - 'a';
        int fromRank = 8 - (uci.charAt(1) - '0');
        int toFile = uci.charAt(2) - 'a';
        int toRank = 8 - (uci.charAt(3) - '0');

        char piece = board[fromRank][fromFile];
        board[toRank][toFile] = piece;
        board[fromRank][fromFile] = '.';

        // 升变
        if (uci.length() > 4) {
            char promotion = uci.charAt(4);
            board[toRank][toFile] = whiteTurn ? Character.toUpperCase(promotion) : promotion;
        }

        whiteTurn = !whiteTurn;
    }

    private String extractTargetSquare(String san) {
        // 从 SAN 中提取目标格子（最后两个字符通常是目标格子）
        for (int i = san.length() - 2; i >= 0; i--) {
            char c1 = san.charAt(i);
            char c2 = san.charAt(i + 1);
            if (c1 >= 'a' && c1 <= 'h' && c2 >= '1' && c2 <= '8') {
                return "" + c1 + c2;
            }
        }
        return null;
    }

    private char extractPiece(String san) {
        char first = san.charAt(0);
        if (first >= 'A' && first <= 'Z') {
            return first;
        }
        // 如果是小写字母（兵的走法），返回 P
        return 'P';
    }

    private String findFromSquare(char piece, String san, String targetSquare) {
        int targetFile = targetSquare.charAt(0) - 'a';
        // int targetRank = 8 - (targetSquare.charAt(1) - '0'); // 未来版本可能需要

        // 简化实现：遍历棋盘查找该棋子
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                if (board[rank][file] == piece) {
                    // 简单验证：兵的走法
                    if (piece == 'P' || piece == 'p') {
                        if (file == targetFile || Math.abs(file - targetFile) == 1) {
                            return String.format("%c%d", (char)('a' + file), 8 - rank);
                        }
                    } else {
                        // 其他棋子（简化处理）
                        return String.format("%c%d", (char)('a' + file), 8 - rank);
                    }
                }
            }
        }
        return null;
    }
}
