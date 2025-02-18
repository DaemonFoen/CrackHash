package com.nsu.manager.service;

import com.nsu.manager.dto.Task;
import com.nsu.manager.model.Worker;
import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Log4j2
public class TaskDispatcherService {

    private final Queue<Task> taskQueue = new ConcurrentLinkedQueue<>();
    private final Queue<Worker> availableWorkers = new ConcurrentLinkedQueue<>();
    private final RestTemplate restTemplate;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Map<String, WorkerWithPayload> workersInProcessing = new ConcurrentHashMap<>();

    public TaskDispatcherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void start() {
        while (true) {
            if (!taskQueue.isEmpty() && !availableWorkers.isEmpty()) {
                Task task = taskQueue.poll();
                Worker worker = availableWorkers.poll();
                if (task != null && worker != null) {
                    assignTask(worker, task);
                }
            }
        }
    }

    @PostConstruct
    public void init() {
        executorService.submit(this::start);
    }

    private void assignTask(Worker worker, Task task) {
        log.info("Отправляем задачу {} на {}", task, worker.url());
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    "http://"+ worker.url() + "/internal/api/worker/hash/crack/task",
                    task,
                    Void.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Ошибка отправки задачи на " + worker.url());
                availableWorkers.offer(worker);
                taskQueue.offer(task);
            }
            log.info("Текущее число задач {}", taskQueue.size());
            workersInProcessing.put(worker.id(), new WorkerWithPayload(worker, task));
        } catch (Exception e) {
            log.error("Ошибка при отправке задачи: " + e.getMessage());
            availableWorkers.offer(worker);
            taskQueue.offer(task);
        }
    }

    public boolean workerIsPresent(String workerId) {
        return workersInProcessing.containsKey(workerId);

    }

    public void releaseWorker(String workerId) {
        log.info("Workers in processing: {}", workersInProcessing);

        if (workerId == null) {
            log.error("WorkerId is null!");
            throw new IllegalStateException("WorkerId is null!");
        }

        WorkerWithPayload r = workersInProcessing.remove(workerId);

        if (r == null) {
            log.error("Worker with id " + workerId + " not found");
        }

        availableWorkers.offer(r.worker);
    }

    public String registerWorker(String workerUrl) {
        String uuid = UUID.randomUUID().toString();
        Worker worker = new Worker(workerUrl, uuid);
        log.info("Register worker: {}", worker);
        availableWorkers.offer(worker);
        log.info("Current available workers: {}", availableWorkers);
        return uuid;
    }

    public Collection<WorkerWithPayload> getWorkersInProcessing() {
        return workersInProcessing.values();
    }

    public void removeDeadWorker(String workerId) {
        var w = workersInProcessing.remove(workerId);
        if (w == null) {
            log.warn("Worker with id {} not found", workerId);
        }
    }

    public void submitTask(Task task) {
        log.info("Submit task: {}", task);
        taskQueue.offer(task);
        log.info("Task queue size: {}", taskQueue.size());
    }

    public List<Worker> getAvailableWorkers() {
        return availableWorkers.stream().toList();
    }

    public record WorkerWithPayload(Worker worker, Task task) {}
}
