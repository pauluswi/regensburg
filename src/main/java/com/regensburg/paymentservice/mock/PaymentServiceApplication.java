package com.regensburg.paymentservice.mock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

// Enable AspectJ auto-proxy for Resilience4j AOP (Circuit Breaker)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
