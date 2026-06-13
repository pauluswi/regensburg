package com.regensburg.paymentservice.mock.event;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class PaymentEvent {
    String transactionId;
    String requestId;
    String sourceAccountNumber;
    String destinationAccountNumber;
    BigDecimal amount;
    String currency;
    String narration;
    String status; // e.g., PROCESSED, FAILED, REVIEW
    @Builder.Default
    Instant timestamp = Instant.now();
}
