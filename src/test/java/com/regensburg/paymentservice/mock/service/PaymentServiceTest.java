package com.regensburg.paymentservice.mock.service;

import com.regensburg.paymentservice.dto.PaymentRequest;
import com.regensburg.paymentservice.mock.event.PaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private RedisIdempotencyService idempotencyService;
    @Mock
    private ExternalFraudDetectionService fraudDetectionService;
    @Mock
    private KafkaProducerService kafkaProducerService;

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
                "USD",
                "Test payment"
        );
    }

    @Test
    void processPayment_success() throws ExecutionException, InterruptedException {
        // Given
        when(idempotencyService.isRequestProcessed(anyString())).thenReturn(false);
        when(fraudDetectionService.checkFraud(any(PaymentRequest.class))).thenReturn(false);

        // Move KafkaProducerService stubbing here as it's only used in this test
        doAnswer(invocation -> {
            PaymentEvent event = invocation.getArgument(0);
            CompletableFuture.runAsync(() -> {
                // Simulate successful async execution
                System.out.println("Mock Kafka send completed for event: " + event.getTransactionId());
            }).get(); // Block until the async task completes for test verification
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

        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(kafkaProducerService, times(1)).sendPaymentEvent(eventCaptor.capture());
        PaymentEvent capturedEvent = eventCaptor.getValue();
        assertEquals(transactionId, capturedEvent.getTransactionId());
        assertEquals("PROCESSED", capturedEvent.getStatus());

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
        verify(idempotencyService, times(1)).markRequestAsFailed(eq(validPaymentRequest.getRequestId()), eq("Fraudulent"));
        verify(kafkaProducerService, never()).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    void processPayment_fraudDetectionServiceFails_fallbackTriggered() {
        // Given
        when(idempotencyService.isRequestProcessed(anyString())).thenReturn(false);
        // Simulate the *outcome* of the fallback: fraudDetectionService returns true (fraudulent)
        when(fraudDetectionService.checkFraud(any(PaymentRequest.class))).thenReturn(true);

        // When
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            paymentService.processPayment(validPaymentRequest);
        });

        // Then
        assertTrue(thrown.getMessage().contains("flagged as fraudulent")); // Fallback marks as fraudulent

        verify(idempotencyService, times(1)).isRequestProcessed(validPaymentRequest.getRequestId());
        verify(idempotencyService, times(1)).markRequestAsProcessing(eq(validPaymentRequest.getRequestId()), anyString());
        verify(fraudDetectionService, times(1)).checkFraud(validPaymentRequest);
        verify(idempotencyService, times(1)).markRequestAsFailed(eq(validPaymentRequest.getRequestId()), eq("Fraudulent"));
        verify(kafkaProducerService, never()).sendPaymentEvent(any(PaymentEvent.class));
    }
}
