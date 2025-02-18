package com.nsu.manager.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Status {
    private final AtomicInteger finishedTasks = new AtomicInteger(0);
    private final int overallTasks;
    @Getter
    private List<String> results;
    private boolean failed = false;

    public String getTaskStatus() {
        if (failed) {
            return "ERROR";
        }
        if (overallTasks == finishedTasks.get()) {
            return "READY";
        }
        return "IN_PROGRESS";
    }

    public void incrementFinishedTasks() {
        finishedTasks.incrementAndGet();
    }

    public void markAsFailed() {
        failed = true;
    }

    public void addResult(List<String> results) {
        if (this.results == null) {
            this.results = new ArrayList<>();
        }
        this.results.addAll(results);
    }
}
