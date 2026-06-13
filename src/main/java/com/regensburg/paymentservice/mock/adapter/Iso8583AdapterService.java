package com.regensburg.paymentservice.mock.adapter;

import com.regensburg.paymentservice.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Mock service simulating an ISO 8583 Adapter.
 * This adapter encapsulates the complexity of ISO 8583 communication
 * and translates internal PaymentRequests into ISO 8583 messages
 * for an external system (e.g., ATM Switch, Payment Network).
 * ADR-016: Integration Adapter Design (for ISO 8583/20022)
 */
@Service
@Slf4j
public class Iso8583AdapterService {

    private final Random random = new Random();

    /**
     * Simulates sending an internal PaymentRequest via ISO 8583 to an external system.
     * @param request The internal PaymentRequest DTO.
     * @return A mock ISO 8583 response code (e.g., "00" for approved).
     */
    public String sendPaymentRequestViaIso8583(PaymentRequest request) {
        log.info("ISO8583 Adapter: Received internal PaymentRequest for processing: {}", request.getRequestId());

        // ADR-016: Data Transformation - Map internal DTO to mock ISO 8583 message
        Iso8583Message isoRequest = mapPaymentRequestToIso8583(request);
        log.info("ISO8583 Adapter: Transformed to mock ISO 8583 request: MTI={}, Amount={}, Account={}",
                isoRequest.getMti(), isoRequest.getTransactionAmount(), isoRequest.getPrimaryAccountNumber());

        // ADR-016: Protocol Handling - Simulate sending and receiving response
        String externalResponseCode = simulateExternalIso8583Communication(isoRequest);
        log.info("ISO8583 Adapter: Received external ISO 8583 response code: {} for request ID: {}",
                externalResponseCode, request.getRequestId());

        // ADR-016: Error Handling - Translate external response to internal status
        if ("00".equals(externalResponseCode)) {
            return "APPROVED";
        } else if ("05".equals(externalResponseCode)) {
            return "DECLINED_DO_NOT_HONOR";
        } else {
            return "FAILED_EXTERNAL_ERROR";
        }
    }

    private Iso8583Message mapPaymentRequestToIso8583(PaymentRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String transmissionDateTime = now.format(DateTimeFormatter.ofPattern("MMddHHmmss"));
        String localTransactionTime = now.format(DateTimeFormatter.ofPattern("HHmmss"));
        String localTransactionDate = now.format(DateTimeFormatter.ofPattern("MMdd"));

        return Iso8583Message.builder()
                .mti("0200") // Financial Transaction Request
                .primaryBitmap("F234567890ABCDEF") // Simplified bitmap
                .processingCode("000000") // Goods and Services
                .transactionAmount(request.getAmount())
                .transmissionDateTime(transmissionDateTime)
                .systemTraceAuditNumber(String.format("%06d", random.nextInt(999999))) // Random 6-digit STAN
                .localTransactionTime(localTransactionTime)
                .localTransactionDate(localTransactionDate)
                .acquiringInstitutionIdCode("123456") // Mock ID
                .forwardingInstitutionIdCode("789012") // Mock ID
                .primaryAccountNumber(request.getSourceAccountNumber())
                .currencyCode(request.getCurrency())
                .build();
    }

    private String simulateExternalIso8583Communication(Iso8583Message isoRequest) {
        // Simulate network latency and external system processing
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(500) + 100); // 100-600ms latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("ISO8583 communication interrupted.", e);
            return "96"; // System malfunction
        }

        // Simulate various response codes
        double outcome = random.nextDouble();
        if (outcome < 0.7) { // 70% success
            return "00"; // Approved
        } else if (outcome < 0.85) { // 15% decline
            return "05"; // Do not honor
        } else { // 15% transient error
            log.warn("ISO8583 Adapter: Simulating transient external error for MTI: {}", isoRequest.getMti());
            return "91"; // Issuer or switch inoperative (transient)
        }
    }
}
