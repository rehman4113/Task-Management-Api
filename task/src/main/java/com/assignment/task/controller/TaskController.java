package com.assignment.task.controller;

import com.assignment.task.dto.TaskRequest;
import com.assignment.task.dto.TaskResponse;
import com.assignment.task.dto.TaskUpdateRequest;
import com.assignment.task.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    //Create task
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @RequestBody TaskRequest request,
            Authentication authentication) {
        String email = authentication.getName(); // user’s email from SecurityContext
        return ResponseEntity.ok(taskService.createTask(request, email));
    }

    //Get all tasks for logged-in user
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(taskService.getTasks(email));
    }

    //Update task
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String id,
            @RequestBody TaskUpdateRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(taskService.updateTask(id, request, email));
    }

    //Delete task
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(
            @PathVariable String id,
            Authentication authentication) {
        String email = authentication.getName();
        taskService.deleteTask(id, email);
        return ResponseEntity.ok("Task deleted successfully");
    }
}
