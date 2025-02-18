package com.nsu.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkerTaskRequest {
    private String hash;
    private int maxLength;
}