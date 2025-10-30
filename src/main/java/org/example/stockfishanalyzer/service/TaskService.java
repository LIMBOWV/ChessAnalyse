package org.example.stockfishanalyzer.service;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.CreateTaskRequest;
import org.example.stockfishanalyzer.dto.TaskDTO;
import org.example.stockfishanalyzer.dto.TaskStatisticsDTO;
import org.example.stockfishanalyzer.entity.Task;
import org.example.stockfishanalyzer.repository.GamePgnRepository;
import org.example.stockfishanalyzer.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final GamePgnRepository gamePgnRepository;
    
    /**
     * 获取用户所有任务
     */
    public List<TaskDTO> getUserTasks(Long userId) {
        List<Task> tasks = taskRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 按状态筛选任务
     */
    public List<TaskDTO> getTasksByStatus(Long userId, String status) {
        List<Task> tasks = taskRepository.findByUserIdAndStatus(userId, status);
        return tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建任务
     */
    @Transactional
    public TaskDTO createTask(Long userId, CreateTaskRequest request) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTaskTitle(request.getTaskTitle());
        task.setTaskType(request.getTaskType());
        task.setPriority(request.getPriority());
        task.setStatus("pending");  // 默认状态为待办
        task.setRelatedGameId(request.getRelatedGameId());
        task.setTargetDate(request.getTargetDate());
        task.setDescription(request.getDescription());
        task.setCreatedAt(LocalDateTime.now());
        
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }
    
    /**
     * 更新任务
     */
    @Transactional
    public TaskDTO updateTask(Long taskId, CreateTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        task.setTaskTitle(request.getTaskTitle());
        task.setTaskType(request.getTaskType());
        task.setPriority(request.getPriority());
        task.setRelatedGameId(request.getRelatedGameId());
        task.setTargetDate(request.getTargetDate());
        task.setDescription(request.getDescription());
        
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }
    
    /**
     * 更新任务状态
     */
    @Transactional
    public TaskDTO updateTaskStatus(Long taskId, String status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        task.setStatus(status);
        
        // 如果标记为完成，记录完成时间
        if ("completed".equals(status)) {
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }
        
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }
    
    /**
     * 删除任务
     */
    @Transactional
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }
    
    /**
     * 获取任务统计
     */
    public TaskStatisticsDTO getTaskStatistics(Long userId) {
        List<Task> allTasks = taskRepository.findByUserId(userId);
        
        TaskStatisticsDTO stats = new TaskStatisticsDTO();
        
        // 总任务数
        stats.setTotalTasks(allTasks.size());
        
        // 按状态统计
        stats.setPendingTasks((int) allTasks.stream()
                .filter(t -> "pending".equals(t.getStatus())).count());
        stats.setInProgressTasks((int) allTasks.stream()
                .filter(t -> "in_progress".equals(t.getStatus())).count());
        stats.setCompletedTasks((int) allTasks.stream()
                .filter(t -> "completed".equals(t.getStatus())).count());
        
        // 今日任务（目标日期是今天的任务）
        LocalDate today = LocalDate.now();
        stats.setTodayTasks((int) allTasks.stream()
                .filter(t -> today.equals(t.getTargetDate())).count());
        
        // 本周任务和完成率
        LocalDate weekStart = today.minus(7, ChronoUnit.DAYS);
        List<Task> weekTasks = allTasks.stream()
                .filter(t -> t.getCreatedAt() != null && 
                        t.getCreatedAt().toLocalDate().isAfter(weekStart))
                .collect(Collectors.toList());
        
        stats.setWeekTasks(weekTasks.size());
        
        long weekCompleted = weekTasks.stream()
                .filter(t -> "completed".equals(t.getStatus())).count();
        stats.setWeekCompletionRate(weekTasks.isEmpty() ? 0.0 : 
                (weekCompleted * 100.0 / weekTasks.size()));
        
        // 按优先级统计
        stats.setHighPriorityTasks((int) allTasks.stream()
                .filter(t -> "high".equals(t.getPriority())).count());
        stats.setMediumPriorityTasks((int) allTasks.stream()
                .filter(t -> "medium".equals(t.getPriority())).count());
        stats.setLowPriorityTasks((int) allTasks.stream()
                .filter(t -> "low".equals(t.getPriority())).count());
        
        // 按类型统计
        stats.setStudyOpeningTasks((int) allTasks.stream()
                .filter(t -> "study_opening".equals(t.getTaskType())).count());
        stats.setAnalyzeGameTasks((int) allTasks.stream()
                .filter(t -> "analyze_game".equals(t.getTaskType())).count());
        stats.setPracticeTasks((int) allTasks.stream()
                .filter(t -> "practice".equals(t.getTaskType())).count());
        
        return stats;
    }
    
    /**
     * 转换为 DTO
     */
    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setUserId(task.getUserId());
        dto.setTaskTitle(task.getTaskTitle());
        dto.setTaskType(task.getTaskType());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setRelatedGameId(task.getRelatedGameId());
        dto.setTargetDate(task.getTargetDate());
        dto.setDescription(task.getDescription());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setCompletedAt(task.getCompletedAt());
        
        // 如果有关联棋局，获取棋局标题
        if (task.getRelatedGameId() != null) {
            gamePgnRepository.findById(task.getRelatedGameId())
                    .ifPresent(game -> {
                        String title = game.getWhitePlayer() + " vs " + game.getBlackPlayer();
                        dto.setRelatedGameTitle(title);
                    });
        }
        
        return dto;
    }
}
