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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TaskRequest taskRequest;

    @InjectMocks
    private TaskService taskService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId("user1");
        user.setEmail("john@example.com");
        user.setName("John");
    }

    // -------- CREATE TASK --------

    @Test
    void createTask_success() {
        // Given
        TaskRequest request = new TaskRequest("Test Task", "Desc", TaskStatus.DONE);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId("task1");
            return task;
        });

        // When
        TaskResponse response = taskService.createTask(request, user.getEmail());

        // Then
        assertNotNull(response);
        assertEquals("task1", response.getId());
        assertEquals("Test Task", response.getTitle());
        assertEquals("Desc", response.getDescription());
        assertEquals(TaskStatus.DONE, response.getStatus());
        assertEquals("user1", response.getUserId());
    }

    @Test
    void createTask_userNotFound() {
        // Given
        when(userRepository.findByEmail("wrong@example.com")).thenReturn(Optional.empty());

        // When
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.createTask(new TaskRequest("T", "D", TaskStatus.OPEN), "wrong@example.com"));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());
    }

    // -------- GET TASKS --------

    @Test
    void getTasks_success() {
        // Given
        Task task1 = new Task("task1", "T1", "D1", TaskStatus.OPEN, "user1");
        Task task2 = new Task("task2", "T2", "D2", TaskStatus.DONE, "user1");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findByUserId("user1")).thenReturn(Arrays.asList(task1, task2));

        // When
        List<TaskResponse> responses = taskService.getTasks(user.getEmail());

        // Then
        assertEquals(2, responses.size());
        assertEquals("task1", responses.get(0).getId());
        assertEquals("task2", responses.get(1).getId());
    }

    @Test
    void getTasks_userNotFound() {
        // Given
        when(userRepository.findByEmail("x@example.com")).thenReturn(Optional.empty());

        // When
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.getTasks("x@example.com"));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());
    }

    // -------- UPDATE TASK --------

    @Test
    void updateTask_success() {
        // Given
        Task task = new Task("task1", "T1", "D1", TaskStatus.OPEN, "user1");
        TaskUpdateRequest request = new TaskUpdateRequest(TaskStatus.DONE);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById("task1")).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        // When
        TaskResponse response = taskService.updateTask("task1", request, user.getEmail());

        // Then
        assertEquals(TaskStatus.DONE, response.getStatus());
    }

    @Test
    void updateTask_userNotFound() {
        // Given
        when(userRepository.findByEmail("x@example.com")).thenReturn(Optional.empty());

        // When
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.updateTask("task1", new TaskUpdateRequest(TaskStatus.OPEN), "x@example.com"));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateTask_taskNotFound() {
        // Given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById("invalid")).thenReturn(Optional.empty());

        // When
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.updateTask("invalid", new TaskUpdateRequest(TaskStatus.OPEN), user.getEmail()));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Task not found", ex.getReason());
    }

    @Test
    void updateTask_forbidden() {
        // Given
        Task task = new Task("task1", "T1", "D1", TaskStatus.OPEN, "otherUser");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById("task1")).thenReturn(Optional.of(task));

        // When
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.updateTask("task1", new TaskUpdateRequest(TaskStatus.OPEN), user.getEmail()));

        // Then
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("You cannot update someone else's task", ex.getReason());
    }

    // -------- DELETE TASK --------

    @Test
    void deleteTask_success() {
        // Given
        Task task = new Task("task1", "T1", "D1", TaskStatus.OPEN, "user1");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById("task1")).thenReturn(Optional.of(task));

        // When
        taskService.deleteTask("task1", user.getEmail());

        // Then
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTask_userNotFound() {
        // Given
        when(userRepository.findByEmail("x@example.com")).thenReturn(Optional.empty());

        // When
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.deleteTask("task1", "x@example.com"));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());
    }

    @Test
    void deleteTask_taskNotFound() {
        // Given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById("notfound")).thenReturn(Optional.empty());

        // When
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.deleteTask("notfound", user.getEmail()));

        // Then
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Task not found", ex.getReason());
    }

    @Test
    void deleteTask_forbidden() {
        // Given
        Task task = new Task("task1", "T1", "D1", TaskStatus.OPEN, "otherUser");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.findById("task1")).thenReturn(Optional.of(task));

        // When
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> taskService.deleteTask("task1", user.getEmail()));

        // Then
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("You cannot delete someone else's task", ex.getReason());
    }
}
