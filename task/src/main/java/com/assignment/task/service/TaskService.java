package com.assignment.task.service;

import com.assignment.task.dto.TaskRequest;
import com.assignment.task.dto.TaskResponse;
import com.assignment.task.dto.TaskUpdateRequest;
import com.assignment.task.model.Task;
import com.assignment.task.model.TaskStatus;
import com.assignment.task.model.User;
import com.assignment.task.repository.TaskRepository;
import com.assignment.task.repository.UserRepository;
import com.assignment.task.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private TaskRepository taskRepository;
    private UserRepository userRepository;
    private JwtUtil jwtUtil;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, JwtUtil jwtUtil) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public TaskResponse createTask(TaskRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.OPEN);
        task.setUserId(user.getId());

        Task saved = taskRepository.save(task);

        return mapToResponse(saved);
    }

    public List<TaskResponse> getTasks(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return taskRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse updateTask(String id, TaskUpdateRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        // ✅ Rule: Prevent accessing another user’s task
        if (!task.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot update someone else's task");
        }

        task.setStatus(request.getStatus());
        Task updated = taskRepository.save(task);

        return mapToResponse(updated);
    }

    public void deleteTask(String id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        // ✅ Rule: Prevent deleting another user’s task
        if (!task.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete someone else's task");
        }

        taskRepository.delete(task);
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getUserId()
        );
    }
}
