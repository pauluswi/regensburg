package com.regensburg.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PaymentRequest {
    @NotBlank(message = "Request ID cannot be blank")
    String requestId; // For idempotency check

    @NotBlank(message = "Source Account Number cannot be blank")
    @Size(min = 10, max = 16, message = "Source Account Number must be between 10 and 16 digits")
    String sourceAccountNumber;

    @NotBlank(message = "Destination Account Number cannot be blank")
    @Size(min = 10, max = 16, message = "Destination Account Number must be between 10 and 16 digits")
    String destinationAccountNumber;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount;

    @NotBlank(message = "Currency cannot be blank")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    String currency;

    String narration;
}
