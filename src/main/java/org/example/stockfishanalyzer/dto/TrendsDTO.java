package org.example.stockfishanalyzer.dto;

import java.util.List;

/**
 * 趋势分析数据传输对象
 */
public class TrendsDTO {
    
    /**
     * 时间点数据
     */
    public static class DataPoint {
        private String date;           // 日期
        private Double avgAccuracy;    // 平均精准度
        private Integer totalGames;    // 棋局数量
        private Double winRate;        // 胜率
        private Integer blunders;      // 漏着总数
        private Integer mistakes;      // 失误总数
        private Integer inaccuracies;  // 不精确总数
        private Integer brilliantMoves; // 妙手总数
        
        public DataPoint() {}
        
        public DataPoint(String date, Double avgAccuracy, Integer totalGames, 
                        Double winRate, Integer blunders, Integer mistakes, 
                        Integer inaccuracies, Integer brilliantMoves) {
            this.date = date;
            this.avgAccuracy = avgAccuracy;
            this.totalGames = totalGames;
            this.winRate = winRate;
            this.blunders = blunders;
            this.mistakes = mistakes;
            this.inaccuracies = inaccuracies;
            this.brilliantMoves = brilliantMoves;
        }
        
        // Getters and Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public Double getAvgAccuracy() { return avgAccuracy; }
        public void setAvgAccuracy(Double avgAccuracy) { this.avgAccuracy = avgAccuracy; }
        
        public Integer getTotalGames() { return totalGames; }
        public void setTotalGames(Integer totalGames) { this.totalGames = totalGames; }
        
        public Double getWinRate() { return winRate; }
        public void setWinRate(Double winRate) { this.winRate = winRate; }
        
        public Integer getBlunders() { return blunders; }
        public void setBlunders(Integer blunders) { this.blunders = blunders; }
        
        public Integer getMistakes() { return mistakes; }
        public void setMistakes(Integer mistakes) { this.mistakes = mistakes; }
        
        public Integer getInaccuracies() { return inaccuracies; }
        public void setInaccuracies(Integer inaccuracies) { this.inaccuracies = inaccuracies; }
        
        public Integer getBrilliantMoves() { return brilliantMoves; }
        public void setBrilliantMoves(Integer brilliantMoves) { this.brilliantMoves = brilliantMoves; }
    }
    
    /**
     * 开局统计
     */
    public static class OpeningStats {
        private String openingName;
        private Integer count;
        private Double winRate;
        
        public OpeningStats() {}
        
        public OpeningStats(String openingName, Integer count, Double winRate) {
            this.openingName = openingName;
            this.count = count;
            this.winRate = winRate;
        }
        
        public String getOpeningName() { return openingName; }
        public void setOpeningName(String openingName) { this.openingName = openingName; }
        
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
        
        public Double getWinRate() { return winRate; }
        public void setWinRate(Double winRate) { this.winRate = winRate; }
    }
    
    /**
     * 总体统计
     */
    public static class OverallStats {
        private Integer totalGames;
        private Double avgAccuracy;
        private Double overallWinRate;
        private Integer totalBlunders;
        private Integer totalMistakes;
        private Integer totalInaccuracies;
        private Integer totalBrilliantMoves;
        
        public OverallStats() {}
        
        public Integer getTotalGames() { return totalGames; }
        public void setTotalGames(Integer totalGames) { this.totalGames = totalGames; }
        
        public Double getAvgAccuracy() { return avgAccuracy; }
        public void setAvgAccuracy(Double avgAccuracy) { this.avgAccuracy = avgAccuracy; }
        
        public Double getOverallWinRate() { return overallWinRate; }
        public void setOverallWinRate(Double overallWinRate) { this.overallWinRate = overallWinRate; }
        
        public Integer getTotalBlunders() { return totalBlunders; }
        public void setTotalBlunders(Integer totalBlunders) { this.totalBlunders = totalBlunders; }
        
        public Integer getTotalMistakes() { return totalMistakes; }
        public void setTotalMistakes(Integer totalMistakes) { this.totalMistakes = totalMistakes; }
        
        public Integer getTotalInaccuracies() { return totalInaccuracies; }
        public void setTotalInaccuracies(Integer totalInaccuracies) { this.totalInaccuracies = totalInaccuracies; }
        
        public Integer getTotalBrilliantMoves() { return totalBrilliantMoves; }
        public void setTotalBrilliantMoves(Integer totalBrilliantMoves) { this.totalBrilliantMoves = totalBrilliantMoves; }
    }
    
    private List<DataPoint> timeline;        // 时间序列数据
    private List<OpeningStats> openings;     // 开局统计
    private OverallStats overall;            // 总体统计
    
    public TrendsDTO() {}
    
    public TrendsDTO(List<DataPoint> timeline, List<OpeningStats> openings, OverallStats overall) {
        this.timeline = timeline;
        this.openings = openings;
        this.overall = overall;
    }
    
    public List<DataPoint> getTimeline() { return timeline; }
    public void setTimeline(List<DataPoint> timeline) { this.timeline = timeline; }
    
    public List<OpeningStats> getOpenings() { return openings; }
    public void setOpenings(List<OpeningStats> openings) { this.openings = openings; }
    
    public OverallStats getOverall() { return overall; }
    public void setOverall(OverallStats overall) { this.overall = overall; }
}
