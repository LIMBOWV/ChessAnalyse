package org.example.stockfishanalyzer.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.example.stockfishanalyzer.dto.StockfishAnalysisResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.*;

/**
 * Stockfish 引擎 IPC 服务
 * 核心职责：通过进程间通信 (IPC) 调用外部 Stockfish 引擎，实现 UCI 协议交互
 */
@Slf4j
@Service
public class StockfishService {

    @Value("${stockfish.engine.path:/usr/local/bin/stockfish}")
    private String enginePath;

    @Value("${stockfish.analysis.movetime:1000}")
    private int moveTime;

    private Process stockfishProcess;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        try {
            log.info("初始化 Stockfish 引擎，路径: {}", enginePath);

            // 检查 Stockfish 引擎是否存在
            java.io.File engineFile = new java.io.File(enginePath);
            if (!engineFile.exists()) {
                log.warn("Stockfish 引擎文件不存在: {}，跳过初始化（可能是测试环境）", enginePath);
                return;
            }

            // 启动 Stockfish 进程
            ProcessBuilder pb = new ProcessBuilder(enginePath);
            stockfishProcess = pb.start();

            // 获取输入输出流
            reader = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(stockfishProcess.getOutputStream()));

            // 创建线程池用于异步读取输出（避免阻塞）
            executorService = Executors.newSingleThreadExecutor();

            // 初始化 UCI 引擎
            sendCommand("uci");
            waitForResponse("uciok", 5000);

            sendCommand("isready");
            waitForResponse("readyok", 5000);

            log.info("Stockfish 引擎初始化成功");
        } catch (Exception e) {
            log.warn("Stockfish 引擎初始化失败: {}，可能是测试环境或引擎未安装", e.getMessage());
            // 不抛出异常，允许应用在没有 Stockfish 的情况下启动（用于测试）
        }
    }

    /**
     * 分析棋局位置
     * @param fen FEN 字符串表示的棋局位置（可选，如果为 null 则使用 moves）
     * @param moves 走法序列（UCI 格式，如 "e2e4 e7e5"）
     * @return 分析结果
     */
    public StockfishAnalysisResult analyzePosition(String fen, String moves) {
        try {
            // 设置棋局位置
            if (fen != null && !fen.isEmpty()) {
                sendCommand("position fen " + fen);
            } else if (moves != null && !moves.isEmpty()) {
                sendCommand("position startpos moves " + moves);
            } else {
                sendCommand("position startpos");
            }

            // 开始分析（使用 movetime 控制分析时间，避免过长等待）
            sendCommand("go movetime " + moveTime);

            // 等待分析完成
            return parseAnalysisResult();

        } catch (Exception e) {
            log.error("分析位置时发生错误", e);
            throw new RuntimeException("Stockfish 分析失败", e);
        }
    }

    /**
     * 发送命令到 Stockfish
     */
    private void sendCommand(String command) throws IOException {
        log.debug("发送命令: {}", command);
        writer.write(command + "\n");
        writer.flush();
    }

    /**
     * 等待特定响应（带超时）
     */
    private void waitForResponse(String expectedResponse, long timeoutMs) throws IOException, TimeoutException {
        Future<String> future = executorService.submit(() -> {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("收到响应: {}", line);
                if (line.contains(expectedResponse)) {
                    return line;
                }
            }
            return null;
        });

        try {
            future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.cancel(true);
            throw new TimeoutException("等待响应超时: " + expectedResponse);
        }
    }

    /**
     * 解析分析结果
     */
    private StockfishAnalysisResult parseAnalysisResult() throws IOException {
        StockfishAnalysisResult result = new StockfishAnalysisResult();
        String lastInfoLine = null;
        String bestMove = null;

        String line;
        while ((line = reader.readLine()) != null) {
            log.debug("收到: {}", line);

            // 解析 info 行（包含评分信息）
            if (line.startsWith("info") && line.contains("score")) {
                lastInfoLine = line;
            }

            // 解析 bestmove 行（分析完成标志）
            if (line.startsWith("bestmove")) {
                String[] parts = line.split(" ");
                if (parts.length >= 2) {
                    bestMove = parts[1];
                }
                break;
            }
        }

        // 解析评分
        if (lastInfoLine != null) {
            parseScore(lastInfoLine, result);
        }

        result.setBestMove(bestMove);
        return result;
    }

    /**
     * 解析评分信息
     * 格式示例: info depth 20 score cp 120 ...
     *          info depth 15 score mate 5 ...
     */
    private void parseScore(String infoLine, StockfishAnalysisResult result) {
        String[] parts = infoLine.split(" ");

        for (int i = 0; i < parts.length; i++) {
            if ("score".equals(parts[i]) && i + 2 < parts.length) {
                String scoreType = parts[i + 1];
                String scoreValue = parts[i + 2];

                if ("cp".equals(scoreType)) {
                    // 厘兵 (centipawn) 评分，转换为标准格式
                    int cp = Integer.parseInt(scoreValue);
                    result.setScore(cp > 0 ? "+" + cp : scoreValue);
                    result.setMate(false);
                } else if ("mate".equals(scoreType)) {
                    // 将死评分
                    int mateIn = Integer.parseInt(scoreValue);
                    result.setScore("M" + Math.abs(mateIn));
                    result.setMate(true);
                    result.setMateIn(mateIn);
                }
                break;
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            log.info("关闭 Stockfish 引擎");

            if (writer != null) {
                sendCommand("quit");
                writer.close();
            }

            if (reader != null) {
                reader.close();
            }

            if (stockfishProcess != null && stockfishProcess.isAlive()) {
                stockfishProcess.destroy();
                stockfishProcess.waitFor(5, TimeUnit.SECONDS);
                if (stockfishProcess.isAlive()) {
                    stockfishProcess.destroyForcibly();
                }
            }

            if (executorService != null) {
                executorService.shutdown();
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            }

            log.info("Stockfish 引擎已关闭");
        } catch (Exception e) {
            log.error("关闭 Stockfish 引擎时发生错误", e);
        }
    }
}
