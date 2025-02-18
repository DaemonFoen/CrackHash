package com.nsu.worker;

import com.nsu.worker.service.BruteForceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Log4j2
public class ServerInitializationListener implements ApplicationListener<ServletWebServerInitializedEvent> {

    private final RestTemplate restTemplate;

    private final BruteForceService bruteForceService;

    @Value("${MANAGER_URL:localhost:8080}")
    private String managerHost;

    public ServerInitializationListener(RestTemplate restTemplate, BruteForceService bruteForceService) {
        this.restTemplate = restTemplate;
        this.bruteForceService = bruteForceService;
    }

    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        var response = restTemplate.postForEntity("http://"+ managerHost +"/internal/api/manager/workers/register",
                port,
                String.class);

        log.info("Worker id: " + response.getBody());
        bruteForceService.setWorkerId(response.getBody());

        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            log.error("Error registering worker");
            throw new RuntimeException("Error registering worker");
        }
    }
}
