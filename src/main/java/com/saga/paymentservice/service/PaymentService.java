package com.saga.paymentservice.service;

import com.saga.paymentservice.event.PaymentCompletedEvent;
import com.saga.paymentservice.event.PaymentFailedEvent;
import com.saga.paymentservice.event.PaymentRequestedEvent;
import com.saga.paymentservice.model.Payment;
import com.saga.paymentservice.model.PaymentStatus;
import com.saga.paymentservice.property.KafkaProperties;
import com.saga.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final PaymentRepository paymentRepository;

    public void processPayment(PaymentRequestedEvent event) {
        log.info("Ödeme süreci başlatıldı: {}", event);

        boolean isSuccess = event.getQuantity() % 2 == 0;

        if (isSuccess) {
            // 1. Veritabanına kaydet
            Payment payment = Payment.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .price(100.0) // Örnek fiyat
                    .status(PaymentStatus.COMPLETED)
                    .build();
            paymentRepository.save(payment);

            // 2. Kafka'ya event gönder
            PaymentCompletedEvent completed = PaymentCompletedEvent.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .build();
            kafkaTemplate.send(kafkaProperties.getTopic().getPaymentCompleted(), completed);
            log.info("payment-completed event gönderildi: {}", completed);

        } else {
            // 1. Veritabanına kaydet
            Payment payment = Payment.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .price(0.0) // Örnek fiyat
                    .status(PaymentStatus.FAILED)
                    .build();
            paymentRepository.save(payment);

            // 2. Kafka'ya event gönder
            PaymentFailedEvent failed = PaymentFailedEvent.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .reason("Yetersiz bakiye (örnek)")
                    .build();
            kafkaTemplate.send(kafkaProperties.getTopic().getPaymentFailed(), failed);
            log.info("payment-failed event gönderildi: {}", failed);
        }
    }
}