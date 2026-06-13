package com.regensburg.paymentservice.mock.service;

import com.regensburg.paymentservice.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ExternalFraudDetectionService {

    private final Random random = new Random();
    private int failureCount = 0;
    private static final int MAX_FAILURES_BEFORE_RECOVERY = 3; // Simulate service recovery

    public boolean checkFraud(PaymentRequest request) {
        log.info("Simulating call to external fraud detection service for request ID: {}", request.getRequestId());

        // Simulate network latency
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(200) + 50); // 50-250ms latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Fraud detection call interrupted", e);
        }

        // Simulate transient failures for Circuit Breaker demonstration
        if (failureCount < MAX_FAILURES_BEFORE_RECOVERY && random.nextDouble() < 0.4) { // 40% chance of failure
            failureCount++;
            log.error("Simulating fraud detection service failure (count: {}) for request ID: {}", failureCount, request.getRequestId());
            throw new RuntimeException("Simulated fraud detection service error");
        } else if (failureCount >= MAX_FAILURES_BEFORE_RECOVERY) {
            // After MAX_FAILURES_BEFORE_RECOVERY, start recovering
            if (random.nextDouble() < 0.8) { // 80% chance of success after failures
                failureCount = 0; // Reset failure count on success
                log.info("Simulating fraud detection service recovery for request ID: {}", request.getRequestId());
                return false; // Not fraudulent
            } else {
                failureCount++;
                log.error("Simulating continued fraud detection service failure (count: {}) for request ID: {}", failureCount, request.getRequestId());
                throw new RuntimeException("Simulated fraud detection service error during recovery");
            }
        }

        // Simulate some requests as fraudulent based on amount
        if (request.getAmount().doubleValue() > 10000.00) {
            log.warn("Request ID {} flagged as potentially fraudulent due to large amount.", request.getRequestId());
            return true; // Fraudulent
        }

        return false; // Not fraudulent
    }
}
