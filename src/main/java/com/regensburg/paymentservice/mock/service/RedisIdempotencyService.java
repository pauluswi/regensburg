package com.regensburg.paymentservice.mock.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisIdempotencyService {

    // Simulate Redis store: requestId -> status/result
    private final Map<String, String> idempotencyStore = new ConcurrentHashMap<>();
    private final Map<String, Long> expiryStore = new ConcurrentHashMap<>(); // Simulate TTL

    private static final long IDEMPOTENCY_KEY_TTL_SECONDS = 300; // 5 minutes

    /**
     * Checks if a request ID has already been processed or is currently being processed.
     * @param requestId The unique request ID.
     * @return true if the request is found in the store and not expired, false otherwise.
     */
    public boolean isRequestProcessed(String requestId) {
        cleanExpiredKeys(); // Simulate Redis expiry
        return idempotencyStore.containsKey(requestId);
    }

    /**
     * Marks a request ID as currently being processed.
     * @param requestId The unique request ID.
     * @param initialStatus An initial status, e.g., "IN_PROGRESS".
     */
    public void markRequestAsProcessing(String requestId, String initialStatus) {
        log.info("Marking request ID {} as IN_PROGRESS with status: {}", requestId, initialStatus);
        idempotencyStore.put(requestId, initialStatus);
        expiryStore.put(requestId, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(IDEMPOTENCY_KEY_TTL_SECONDS));
    }

    /**
     * Marks a request ID as completed with its final result.
     * @param requestId The unique request ID.
     * @param finalResult The final result, e.g., transaction ID.
     */
    public void markRequestAsCompleted(String requestId, String finalResult) {
        log.info("Marking request ID {} as COMPLETED with result: {}", requestId, finalResult);
        idempotencyStore.put(requestId, "COMPLETED:" + finalResult);
        // In a real Redis, you might extend TTL or move to a different key space
    }

    /**
     * Marks a request ID as failed.
     * @param requestId The unique request ID.
     * @param reason The reason for failure.
     */
    public void markRequestAsFailed(String requestId, String reason) {
        log.warn("Marking request ID {} as FAILED with reason: {}", requestId, reason);
        idempotencyStore.put(requestId, "FAILED:" + reason);
        // Keep failed requests for a short period for debugging/analysis
    }

    // Simple expiry cleaner for simulation purposes
    private void cleanExpiredKeys() {
        long currentTime = System.currentTimeMillis();
        expiryStore.entrySet().removeIf(entry -> {
            if (entry.getValue() < currentTime) {
                idempotencyStore.remove(entry.getKey());
                log.debug("Idempotency key {} expired and removed.", entry.getKey());
                return true;
            }
            return false;
        });
    }
}
