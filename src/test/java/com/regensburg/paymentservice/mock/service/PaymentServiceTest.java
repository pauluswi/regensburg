package com.regensburg.paymentservice.mock.service;

import com.regensburg.paymentservice.dto.PaymentRequest;
import com.regensburg.paymentservice.mock.adapter.Iso8583AdapterService;
import com.regensburg.paymentservice.mock.event.PaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceTest.class);

    @Mock
    private RedisIdempotencyService idempotencyService;
    @Mock
    private ExternalFraudDetectionService fraudDetectionService;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private Iso8583AdapterService iso8583AdapterService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest validPaymentRequest;

    @BeforeEach
    void setUp() {
        validPaymentRequest = new PaymentRequest(
                "req123",
                "1234567890",
                "0987654321",
                new BigDecimal("100.00"),
                "EUR", // Changed from USD to EUR
                "Test payment"
        );
    }

    @Test
    void processPayment_success() throws ExecutionException, InterruptedException {
        // Given
        when(idempotencyService.isRequestProcessed(anyString())).thenReturn(false);
        when(fraudDetectionService.checkFraud(any(PaymentRequest.class))).thenReturn(false);
        when(iso8583AdapterService.sendPaymentRequestViaIso8583(any(PaymentRequest.class))).thenReturn("APPROVED"); // Stub here
        doAnswer(invocation -> { // Stub here
            PaymentEvent event = invocation.getArgument(0);
            CompletableFuture.runAsync(() -> {
                log.info("Mock Kafka send completed for event: {}", event.getTransactionId());
            }).get();
            return null;
        }).when(kafkaProducerService).sendPaymentEvent(any(PaymentEvent.class));

        // When
        String transactionId = paymentService.processPayment(validPaymentRequest);

        // Then
        assertNotNull(transactionId);
        assertFalse(transactionId.isEmpty());

        verify(idempotencyService, times(1)).isRequestProcessed(validPaymentRequest.getRequestId());
        verify(idempotencyService, times(1)).markRequestAsProcessing(validPaymentRequest.getRequestId(), transactionId);
        verify(fraudDetectionService, times(1)).checkFraud(validPaymentRequest);
        verify(iso8583AdapterService, times(1)).sendPaymentRequestViaIso8583(validPaymentRequest); // Verify adapter call

        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(kafkaProducerService, times(1)).sendPaymentEvent(eventCaptor.capture());
        PaymentEvent capturedEvent = eventCaptor.getValue();
        assertEquals(transactionId, capturedEvent.getTransactionId());
        assertEquals("PROCESSED", capturedEvent.getStatus());
        assertEquals("EUR", capturedEvent.getCurrency()); // Verify currency in event
        verify(idempotencyService, times(1)).markRequestAsCompleted(validPaymentRequest.getRequestId(), transactionId);
        verify(idempotencyService, never()).markRequestAsFailed(anyString(), anyString());
    }

    @Test
    void processPayment_duplicateRequest_throwsException() {
        // Given
        when(idempotencyService.isRequestProcessed(anyString())).thenReturn(true);

        // When / Then
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            paymentService.processPayment(validPaymentRequest);
        });
        assertTrue(thrown.getMessage().contains("Duplicate request ID"));

        verify(idempotencyService, times(1)).isRequestProcessed(validPaymentRequest.getRequestId());
        verify(idempotencyService, never()).markRequestAsProcessing(anyString(), anyString());
        verify(fraudDetectionService, never()).checkFraud(any(PaymentRequest.class));
        verify(iso8583AdapterService, never()).sendPaymentRequestViaIso8583(any(PaymentRequest.class));
        verify(kafkaProducerService, never()).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    void processPayment_fraudulentRequest_throwsException() {
        // Given
        when(idempotencyService.isRequestProcessed(anyString())).thenReturn(false);
        when(fraudDetectionService.checkFraud(any(PaymentRequest.class))).thenReturn(true);

        // When / Then
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            paymentService.processPayment(validPaymentRequest);
        });
        assertTrue(thrown.getMessage().contains("flagged as fraudulent"));

        verify(idempotencyService, times(1)).isRequestProcessed(validPaymentRequest.getRequestId());
        verify(idempotencyService, times(1)).markRequestAsProcessing(eq(validPaymentRequest.getRequestId()), anyString());
        verify(fraudDetectionService, times(1)).checkFraud(validPaymentRequest);
        verify(iso8583AdapterService, never()).sendPaymentRequestViaIso8583(any(PaymentRequest.class));
        verify(idempotencyService, times(1)).markRequestAsFailed(eq(validPaymentRequest.getRequestId()), eq("Fraudulent"));
        verify(kafkaProducerService, never()).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    void processPayment_fraudDetectionServiceFails_fallbackTriggered() {
        // Given
        when(idempotencyService.isRequestProcessed(anyString())).thenReturn(false);
        when(fraudDetectionService.checkFraud(any(PaymentRequest.class))).thenReturn(true);

        // When
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            paymentService.processPayment(validPaymentRequest);
        });

        // Then
        assertTrue(thrown.getMessage().contains("flagged as fraudulent"));

        verify(idempotencyService, times(1)).isRequestProcessed(validPaymentRequest.getRequestId());
        verify(idempotencyService, times(1)).markRequestAsProcessing(eq(validPaymentRequest.getRequestId()), anyString());
        verify(fraudDetectionService, times(1)).checkFraud(validPaymentRequest);
        verify(iso8583AdapterService, never()).sendPaymentRequestViaIso8583(any(PaymentRequest.class));
        verify(idempotencyService, times(1)).markRequestAsFailed(eq(validPaymentRequest.getRequestId()), eq("Fraudulent"));
        verify(kafkaProducerService, never()).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    void processPayment_iso8583AdapterDeclines_throwsException() {
        // Given
        when(idempotencyService.isRequestProcessed(anyString())).thenReturn(false);
        when(fraudDetectionService.checkFraud(any(PaymentRequest.class))).thenReturn(false);
        when(iso8583AdapterService.sendPaymentRequestViaIso8583(any(PaymentRequest.class))).thenReturn("DECLINED_DO_NOT_HONOR"); // Stub here

        // When / Then
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            paymentService.processPayment(validPaymentRequest);
        });
        assertTrue(thrown.getMessage().contains("Payment declined by core banking system"));

        verify(idempotencyService, times(1)).isRequestProcessed(validPaymentRequest.getRequestId());
        verify(idempotencyService, times(1)).markRequestAsProcessing(eq(validPaymentRequest.getRequestId()), anyString());
        verify(fraudDetectionService, times(1)).checkFraud(validPaymentRequest);
        verify(iso8583AdapterService, times(1)).sendPaymentRequestViaIso8583(validPaymentRequest);
        verify(idempotencyService, times(1)).markRequestAsFailed(eq(validPaymentRequest.getRequestId()), eq("Adapter declined: DECLINED_DO_NOT_HONOR"));
        verify(kafkaProducerService, never()).sendPaymentEvent(any(PaymentEvent.class));
    }
}
