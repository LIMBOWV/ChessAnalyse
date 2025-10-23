package org.example.stockfishanalyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Stockfish 引擎配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "stockfish")
public class StockfishProperties {

    /**
     * 引擎配置
     */
    private Engine engine = new Engine();

    /**
     * 分析配置
     */
    private Analysis analysis = new Analysis();

    @Data
    public static class Engine {
        /**
         * Stockfish 引擎可执行文件路径
         */
        private String path = "/usr/local/bin/stockfish";
    }

    @Data
    public static class Analysis {
        /**
         * 分析时间（毫秒）
         */
        private Integer movetime = 1000;

        /**
         * 分析深度
         */
        private Integer depth = 18;
    }
}

