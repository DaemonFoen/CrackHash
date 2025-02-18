package com.nsu.manager.service;

import com.nsu.manager.dto.Task;
import com.nsu.manager.model.Status;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TaskRegistryService {

    private final List<Character> alphabet;
    private final int partCount;
    private final TaskDispatcherService taskDispatcherService;
    private final Map<String, Status> requestResults = new ConcurrentHashMap<>();

    public TaskRegistryService(
            @Value("${hashcracker.alphabet}") String alphabetString,
            @Value("${hashcracker.partCount}") int partCount, TaskDispatcherService taskDispatcherService) {

        this.alphabet = alphabetString.chars()
                .mapToObj(c -> (char) c)
                .toList();
        this.partCount = partCount;
        this.taskDispatcherService = taskDispatcherService;
    }

    public String registerTasks( String hash, int maxLength) {
        String requestId = UUID.randomUUID().toString();
        requestResults.put(requestId, new Status(partCount));

        for (int partNumber = 0; partNumber < partCount; partNumber++) {
            Task task = new Task(requestId, hash, maxLength, partNumber, partCount, alphabet);
            taskDispatcherService.submitTask(task);
        }

        return requestId;
    }

    public Status getRequestStatus(String requestId) {
        return requestResults.get(requestId);
    }
}
