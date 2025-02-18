package com.nsu.manager.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Log4j2
public class WorkerHealthChecker {

    private final RestTemplate restTemplate;
    private final TaskDispatcherService taskDispatcherService;

    public WorkerHealthChecker(RestTemplate restTemplate, TaskDispatcherService taskDispatcherService) {
        this.restTemplate = restTemplate;
        this.taskDispatcherService = taskDispatcherService;
    }

    @Scheduled(fixedRateString = "${manager.heartbeat.check.interval}")
    public void checkWorkersHealth() {
        for (var i : taskDispatcherService.getAvailableWorkers()) {
            try {
                log.info("Healthcheck for free worker with id: {} with URL: {}", i.id(), i.url());
                ResponseEntity<Void> response = restTemplate.getForEntity("http://"+i.url()+"/internal/api/worker/hash/crack/heartbeat", Void.class);
                boolean isUp = response.getStatusCode().is2xxSuccessful();
                if (!isUp) {
                    log.error("Free worker {} with URL {} is dead", i.id(), i.url());
                }
            } catch (Exception e) {
                log.error("Exception when free worker {} with URL {} was checked", i.id(), i.url());
            }
        }

        for (var i : taskDispatcherService.getWorkersInProcessing()) {
            try {
                log.info("Healthcheck for {} worker with URL: {}", i.worker().id(), i.worker().url());
                ResponseEntity<Void> response = restTemplate.getForEntity("http://"+i.worker().url()+"/internal/api/worker/hash/crack/heartbeat", Void.class);
                boolean isUp = response.getStatusCode().is2xxSuccessful();
                if (!isUp) {
                    log.error("Worker {} with URL {} is dead", i.worker().id(), i.worker().url());
                    taskDispatcherService.submitTask(i.task());
                    taskDispatcherService.removeDeadWorker(i.worker().id());
                }
            } catch (Exception e) {
                log.error("Exception when worker {} with URL {} was checked", i.worker().id(), i.worker().url());
                taskDispatcherService.submitTask(i.task());
                taskDispatcherService.removeDeadWorker(i.worker().id());
            }
        }
    }
}
