package com.nsu.worker.service;

import com.nsu.worker.dto.Task;
import com.nsu.worker.dto.WorkerResponse;
import com.nsu.worker.model.LazyWordGenerator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Log4j2
public class BruteForceService {

    @Getter
    @Setter
    private String workerId;
    private final RestTemplate restTemplate;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MessageDigest md = MessageDigest.getInstance("MD5");
    @Value("${MANAGER_URL:localhost:8080}")
    private String managerHost;

    public BruteForceService(RestTemplate restTemplate) throws NoSuchAlgorithmException {
        this.restTemplate = restTemplate;
    }

    private void bruteForce(Task task) {
        LazyWordGenerator generator = new LazyWordGenerator(task.getAlphabet(), task.getMaxLength(),
                task.getPartCount(), task.getPartNumber());
        byte[] hash = hexToBytes(task.getHash());
        List<String> result = generator.generateWords().filter(e -> Arrays.equals(md.digest(e.getBytes(StandardCharsets.UTF_8)), hash)).toList();
        log.info("Worker found hash collisions : " + result);

        WorkerResponse response = new WorkerResponse(task.getRequestId(), workerId, result);
        log.info("Response: " + response);
        restTemplate.patchForObject("http://"+managerHost+"/internal/api/manager/hash/crack/request", response,
                Void.class);
    }

    public void submitTask(Task task) {
        executorService.submit(() -> bruteForce(task));
    }

    public static byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] bytes = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static String byteToHex(byte b) {
        char[] hexChars = new char[2];
        hexChars[0] = HEX_ARRAY[(b >> 4) & 0xF];
        hexChars[1] = HEX_ARRAY[b & 0xF];
        return String.valueOf(hexChars);
    }

}
