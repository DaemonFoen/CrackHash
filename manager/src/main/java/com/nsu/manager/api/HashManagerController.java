package com.nsu.manager.api;

import com.nsu.manager.dto.HashCrackRequest;
import com.nsu.manager.dto.HashCrackStatusResponse;
import com.nsu.manager.model.Status;
import com.nsu.manager.service.TaskRegistryService;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/hash", produces = "application/json")
public class HashManagerController {

    private final TaskRegistryService taskRegistryService;

    public HashManagerController(TaskRegistryService taskRegistryService) {
        this.taskRegistryService = taskRegistryService;
    }

    @PostMapping("/crack")
    public ResponseEntity<Map<String, String>> submitCrackRequest(@RequestBody HashCrackRequest request) {
        String requestId = taskRegistryService.registerTasks(request.getHash(), request.getMaxLength());
        return ResponseEntity.ok(Collections.singletonMap("requestId", requestId));
    }

    @GetMapping("/status")
    public ResponseEntity<HashCrackStatusResponse> getRequestStatus(@RequestParam String requestId) {
        Status status = taskRegistryService.getRequestStatus(requestId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new HashCrackStatusResponse(status.getTaskStatus(), status.getResults()));
    }
}
