package com.assignment.task.dto;

import com.assignment.task.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TaskRequest {
    private String title;
    private String description;
    private TaskStatus status;
}
