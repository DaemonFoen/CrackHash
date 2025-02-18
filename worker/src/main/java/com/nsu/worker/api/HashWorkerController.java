package com.nsu.worker.api;

import com.nsu.worker.dto.Task;
import com.nsu.worker.service.BruteForceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/api/worker/hash")
@Log4j2
public class HashWorkerController {

    private final BruteForceService bruteForceService;
    
    public HashWorkerController(BruteForceService bruteForceService) {
        this.bruteForceService = bruteForceService;
    }

    @PostMapping("/crack/task")
    public ResponseEntity<Void> processCrackTask(@RequestBody Task request) {
        bruteForceService.submitTask(request);
        log.info("Worker with id: " + bruteForceService.getWorkerId() + "receive task");
        return ResponseEntity.ok().build();
    }

}
