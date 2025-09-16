package com.assignment.task.controller;

import com.assignment.task.dto.TaskRequest;
import com.assignment.task.dto.TaskResponse;
import com.assignment.task.dto.TaskUpdateRequest;
import com.assignment.task.model.TaskStatus;
import com.assignment.task.security.JwtAuthenticationFilter;
import com.assignment.task.security.JwtUtil;
import com.assignment.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter; // 👈 also mock filter if needed

    @Autowired
    private ObjectMapper objectMapper;

    private Authentication auth;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        auth = new UsernamePasswordAuthenticationToken("john@example.com", null);
        taskResponse = new TaskResponse("task1", "Test Task", "Description", TaskStatus.OPEN, "user1");
    }

    // -------- CREATE TASK (positive) --------
    @Test
    void createTask_success() throws Exception {
        TaskRequest request = new TaskRequest("Test Task", "Description", TaskStatus.OPEN);

        Mockito.when(taskService.createTask(any(TaskRequest.class), eq("john@example.com")))
                .thenReturn(taskResponse);

        mockMvc.perform(post("/tasks")
                        .principal(auth) // Mock Authentication
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("task1"))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.userId").value("user1"));
    }

    // -------- GET TASKS (positive) --------
    @Test
    void getTasks_success() throws Exception {
        List<TaskResponse> responses = Arrays.asList(taskResponse);

        Mockito.when(taskService.getTasks("john@example.com")).thenReturn(responses);

        mockMvc.perform(get("/tasks")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("task1"))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    // -------- UPDATE TASK (positive) --------
    @Test
    void updateTask_success() throws Exception {
        TaskUpdateRequest updateRequest = new TaskUpdateRequest(TaskStatus.DONE);
        TaskResponse updatedResponse = new TaskResponse("task1", "Test Task", "Description", TaskStatus.DONE, "user1");

        Mockito.when(taskService.updateTask(eq("task1"), any(TaskUpdateRequest.class), eq("john@example.com")))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/tasks/task1")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("task1"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    // -------- DELETE TASK (positive) --------
    @Test
    void deleteTask_success() throws Exception {
        Mockito.doNothing().when(taskService).deleteTask("task1", "john@example.com");

        mockMvc.perform(delete("/tasks/task1")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(content().string("Task deleted successfully"));
    }
}
