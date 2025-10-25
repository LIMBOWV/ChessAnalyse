package org.example.stockfishanalyzer.controller;

import lombok.RequiredArgsConstructor;
import org.example.stockfishanalyzer.dto.CreateTaskRequest;
import org.example.stockfishanalyzer.dto.TaskDTO;
import org.example.stockfishanalyzer.dto.TaskStatisticsDTO;
import org.example.stockfishanalyzer.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskService taskService;
    
    /**
     * 获取用户所有任务
     */
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getUserTasks(@RequestParam Long userId) {
        List<TaskDTO> tasks = taskService.getUserTasks(userId);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * 按状态筛选任务
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskDTO>> getTasksByStatus(
            @RequestParam Long userId,
            @PathVariable String status) {
        List<TaskDTO> tasks = taskService.getTasksByStatus(userId, status);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * 创建任务
     */
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
            @RequestParam Long userId,
            @RequestBody CreateTaskRequest request) {
        TaskDTO task = taskService.createTask(userId, request);
        return ResponseEntity.ok(task);
    }
    
    /**
     * 更新任务
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long taskId,
            @RequestBody CreateTaskRequest request) {
        TaskDTO task = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(task);
    }
    
    /**
     * 更新任务状态
     */
    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        TaskDTO task = taskService.updateTaskStatus(taskId, status);
        return ResponseEntity.ok(task);
    }
    
    /**
     * 删除任务
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取任务统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<TaskStatisticsDTO> getTaskStatistics(@RequestParam Long userId) {
        TaskStatisticsDTO statistics = taskService.getTaskStatistics(userId);
        return ResponseEntity.ok(statistics);
    }
}
