package com.assignment.task.dto;

import com.assignment.task.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TaskUpdateRequest {
    private TaskStatus status;
}
