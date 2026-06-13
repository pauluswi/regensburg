package com.regensburg.paymentservice.mock;

import com.regensburg.paymentservice.dto.PaymentRequest;
import com.regensburg.paymentservice.mock.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments") // ADR-002: URI Versioning
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<String> processPayment(@Valid @RequestBody PaymentRequest request) { // ADR-002: JSON as Primary Format, Validation
        log.info("Received payment request: {}", request.getRequestId());
        try {
            String transactionId = paymentService.processPayment(request);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Payment request " + transactionId + " accepted for processing."); // ADR-002: Standard HTTP Status Codes
        } catch (IllegalStateException e) {
            log.warn("Payment request failed due to: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // ADR-002: Consistent Error Response Format (simplified here)
        } catch (Exception e) {
            log.error("Error processing payment request {}: {}", request.getRequestId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred."); // ADR-002: Consistent Error Response Format
        }
    }
}
