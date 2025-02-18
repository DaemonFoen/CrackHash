package com.nsu.manager.api;

import com.nsu.manager.dto.WorkerResponse;
import com.nsu.manager.model.Status;
import com.nsu.manager.service.TaskDispatcherService;
import com.nsu.manager.service.TaskRegistryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/internal/api/manager")
public class WorkerManagerController {

    private final TaskDispatcherService taskDispatcherService;

    private final TaskRegistryService taskRegistryService;

    public WorkerManagerController(TaskDispatcherService taskDispatcherService,
            TaskRegistryService taskRegistryService) {
        this.taskDispatcherService = taskDispatcherService;
        this.taskRegistryService = taskRegistryService;
    }

    @PostMapping(value = "/workers/register")
    public ResponseEntity<String> registerWorker(@RequestBody int port, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        log.info("Get request from {}:{} worker", clientIp, port);
        String uuid = taskDispatcherService.registerWorker("%s:%s".formatted(clientIp, port));
        return ResponseEntity.ok().body(uuid);
    }

    @PatchMapping("/hash/crack/request")
    public ResponseEntity<Void> receiveWorkerResult(@RequestBody WorkerResponse workerResponse) {
        log.info("Receive worker result from {}", workerResponse);

        if (!taskDispatcherService.workerIsPresent(workerResponse.getWorkerId())) {
            log.info("Worker with id {} does not exist", workerResponse.getWorkerId());
            return ResponseEntity.ok().build();
        }

        String requestId = workerResponse.getRequestId();
        Status status = taskRegistryService.getRequestStatus(requestId);
        if (status == null) {
            log.fatal("Received null status from {}", requestId);
            throw new IllegalStateException("Received null status from " + requestId);
        }
        status.addResult(workerResponse.getData());
        status.incrementFinishedTasks();

        taskDispatcherService.releaseWorker(workerResponse.getWorkerId());
        log.info("Task {} completed successfully. Data : {}", requestId, workerResponse.getData());
        return ResponseEntity.ok().build();
    }

}
