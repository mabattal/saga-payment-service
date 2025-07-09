package com.saga.paymentservice.consumer;

import com.saga.paymentservice.event.PaymentRequestedEvent;
import com.saga.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestListener {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "${kafka.topic.paymentRequested}",
            groupId = "${kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(PaymentRequestedEvent event) {
        log.info("payment-requested event alındı: {}", event);
        paymentService.processPayment(event);
    }
}