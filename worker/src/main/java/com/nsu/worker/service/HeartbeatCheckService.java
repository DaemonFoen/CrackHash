package com.nsu.worker.service;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Setter
@Service
@Log4j2
public class HeartbeatCheckService {

    private LocalDateTime lastHeartbeat = LocalDateTime.now();
    private final ApplicationContext context;
    private final long heartbeatInterval;

    public HeartbeatCheckService(ApplicationContext context, @Value("${worker.heartbeat.check.interval}") long heartbeatInterval) {
        this.context = context;
        this.heartbeatInterval = heartbeatInterval;
    }

    @Scheduled(fixedRateString = "${worker.heartbeat.check.interval}")
    public void checkInactiveWorkers() {
        log.info("Worker heartbeat check initiated in {}", LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();
        if (Duration.between(lastHeartbeat, now).toMillis() > heartbeatInterval) {
            log.fatal("Worker is DOWN");
            SpringApplication.exit(context, () -> 1);
        }
    }
}
