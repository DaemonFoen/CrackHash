package com.nsu.worker.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkerResponse {
    private String requestId;
    private String workerId;
    private List<String> data;
}
