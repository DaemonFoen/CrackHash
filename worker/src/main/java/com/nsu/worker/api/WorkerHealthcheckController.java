package com.nsu.worker.api;

import com.nsu.worker.service.HeartbeatCheckService;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/api/worker/hash/crack")
public class WorkerHealthcheckController {

    private final HeartbeatCheckService heartbeatCheckService;

    public WorkerHealthcheckController(HeartbeatCheckService heartbeatCheckService) {
        this.heartbeatCheckService = heartbeatCheckService;
    }

    @GetMapping("/heartbeat")
    public ResponseEntity<Void> receiveHeartbeat() {
        heartbeatCheckService.setLastHeartbeat(LocalDateTime.now());
        return ResponseEntity.ok().build();
    }
}
