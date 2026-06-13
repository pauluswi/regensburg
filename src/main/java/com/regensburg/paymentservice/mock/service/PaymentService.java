package com.regensburg.paymentservice.mock.service;

import com.regensburg.paymentservice.dto.PaymentRequest;
import com.regensburg.paymentservice.mock.adapter.Iso8583AdapterService; // Import the new adapter
import com.regensburg.paymentservice.mock.event.PaymentEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RedisIdempotencyService idempotencyService;
    private final ExternalFraudDetectionService fraudDetectionService;
    private final KafkaProducerService kafkaProducerService;
    private final Iso8583AdapterService iso8583AdapterService; // Inject the ISO 8583 adapter

    private static final String FRAUD_DETECTION_SERVICE = "fraudDetectionService"; // Name for Circuit Breaker

    @Retry(name = "paymentProcessingRetry") // ADR-008: Retry Pattern
    public String processPayment(PaymentRequest request) {
        // ADR-005: Idempotency Check using Redis
        if (idempotencyService.isRequestProcessed(request.getRequestId())) {
            log.warn("Duplicate request ID detected: {}. Returning previously processed result.", request.getRequestId());
            // In a real scenario, you'd return the result of the previous successful processing
            throw new IllegalStateException("Duplicate request ID. Payment already processed or is in progress.");
        }

        String transactionId = UUID.randomUUID().toString();
        log.info("Processing payment request {} with generated transaction ID: {}", request.getRequestId(), transactionId);

        // Mark request as in-progress in Redis (ADR-005)
        idempotencyService.markRequestAsProcessing(request.getRequestId(), transactionId);

        // ADR-008: Circuit Breaker for external call
        // ADR-003: Synchronous call to external service
        boolean isFraudulent = callFraudDetectionService(request);

        if (isFraudulent) {
            log.warn("Payment request {} flagged as fraudulent. Transaction ID: {}", request.getRequestId(), transactionId);
            idempotencyService.markRequestAsFailed(request.getRequestId(), "Fraudulent"); // Mark as failed
            throw new IllegalStateException("Payment flagged as fraudulent.");
        }

        // ADR-016: Integrate with ISO 8583 Adapter for core banking interaction
        log.info("Calling ISO 8583 Adapter for core banking transaction for request ID: {}", request.getRequestId());
        String adapterResponseStatus = iso8583AdapterService.sendPaymentRequestViaIso8583(request);

        if (!"APPROVED".equals(adapterResponseStatus)) {
            log.warn("Payment request {} (Tx ID: {}) declined by ISO 8583 adapter with status: {}", request.getRequestId(), transactionId, adapterResponseStatus);
            idempotencyService.markRequestAsFailed(request.getRequestId(), "Adapter declined: " + adapterResponseStatus);
            throw new IllegalStateException("Payment declined by core banking system: " + adapterResponseStatus);
        }

        log.info("Payment request {} (Tx ID: {}) approved by core banking system via ISO 8583 adapter.", request.getRequestId(), transactionId);

        // ADR-003 & ADR-004: Publish event to Kafka for asynchronous processing
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .transactionId(transactionId)
                .requestId(request.getRequestId())
                .sourceAccountNumber(request.getSourceAccountNumber())
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .narration(request.getNarration())
                .status("PROCESSED")
                .build();

        // Asynchronous event publishing
        CompletableFuture.runAsync(() -> {
            try {
                kafkaProducerService.sendPaymentEvent(paymentEvent);
                log.info("Payment event for Tx ID {} published to Kafka.", transactionId);
                idempotencyService.markRequestAsCompleted(request.getRequestId(), transactionId); // Mark as completed after event published
            } catch (Exception e) {
                log.error("Failed to publish payment event for Tx ID {}: {}", transactionId, e.getMessage());
                idempotencyService.markRequestAsFailed(request.getRequestId(), "Kafka publish failed"); // Mark as failed
                // ADR-008: Consider DLQ or other recovery for failed Kafka publish
            }
        });

        return transactionId;
    }

    @CircuitBreaker(name = FRAUD_DETECTION_SERVICE, fallbackMethod = "fraudDetectionFallback") // ADR-008: Circuit Breaker
    private boolean callFraudDetectionService(PaymentRequest request) {
        log.info("Calling external fraud detection service for request ID: {}", request.getRequestId());
        return fraudDetectionService.checkFraud(request);
    }

    // Fallback method for the Circuit Breaker
    private boolean fraudDetectionFallback(PaymentRequest request, Throwable t) {
        log.error("Fraud detection service is unavailable or failed for request ID: {}. Falling back to default (non-fraudulent) or blocking.", request.getRequestId(), t.getMessage());
        // ADR-008: Graceful Degradation - In a real banking system, this might mean:
        // 1. Blocking the transaction (return true for fraudulent)
        // 2. Allowing it but flagging for manual review
        // 3. Using a cached risk score
        // For this mock, we'll simulate blocking for safety.
        return true; // Assume fraudulent or block transaction if fraud service is down
    }
}
