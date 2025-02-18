package com.nsu.manager.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HashCrackStatusResponse {
    private String status;
    private List<String> data;
}