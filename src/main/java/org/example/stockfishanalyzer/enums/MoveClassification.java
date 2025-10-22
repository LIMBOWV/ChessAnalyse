package org.example.stockfishanalyzer.enums;

/**
 * 走法分类枚举 - 启发式算法的输出结果
 */
public enum MoveClassification {
    BRILLIANT,    // 妙手 - 最佳走法且远优于次佳
    BEST,         // 最佳走法
    GOOD,         // 好棋
    INACCURACY,   // 不够精确
    MISTAKE,      // 失误
    BLUNDER       // 大漏着
}
