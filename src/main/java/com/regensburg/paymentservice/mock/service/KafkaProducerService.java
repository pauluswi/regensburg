package com.regensburg.paymentservice.mock.service;

import com.regensburg.paymentservice.mock.event.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class KafkaProducerService {

    private final Random random = new Random();

    public void sendPaymentEvent(PaymentEvent event) {
        log.info("Attempting to send PaymentEvent for Tx ID {} to Kafka...", event.getTransactionId());

        // Simulate Kafka producer latency and occasional failure
        try {
            TimeUnit.MILLISECONDS.sleep(random.nextInt(100) + 20); // 20-120ms latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Kafka send interrupted", e);
        }

        if (random.nextDouble() < 0.05) { // Simulate 5% chance of Kafka send failure
            log.error("Simulated Kafka send failure for Tx ID: {}", event.getTransactionId());
            throw new RuntimeException("Simulated Kafka producer error");
        }

        log.info("Successfully sent PaymentEvent for Tx ID {} to Kafka. Event details: {}", event.getTransactionId(), event);
        // In a real application, this would use KafkaTemplate.send()
    }
}
