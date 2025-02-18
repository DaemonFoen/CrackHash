package com.nsu.manager.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Task {
    private final String requestId;
    private final String hash;
    private final int maxLength;
    private final int partNumber;
    private final int partCount;
    private final List<Character> alphabet;
}
