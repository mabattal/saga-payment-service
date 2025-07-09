package com.saga.paymentservice.consumer;

import com.saga.paymentservice.event.PaymentRequestedEvent;
import com.saga.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestListener {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "${spring.kafka.topic.paymentRequested}",
            groupId = "${spring.kafka.group-id}",
            containerFactory = "paymentRequestedFactory"
    )
    public void listen(PaymentRequestedEvent event, Acknowledgment ack) {
        try {
            log.info("payment-requested event alındı: {}", event);
            paymentService.processPayment(event);
            ack.acknowledge();
        }
        catch (Exception e) {
            log.error("payment-requested event işlenirken hata oluştu: {}", e.getMessage(), e);
            // Hata durumunda offset commit edilmemesi için ack.acknowledge() çağrılmıyor
        }
    }
}