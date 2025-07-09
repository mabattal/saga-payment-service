package com.saga.paymentservice.consumer;

import com.saga.paymentservice.event.RefundPaymentEvent;
import com.saga.paymentservice.model.Payment;
import com.saga.paymentservice.model.PaymentStatus;
import com.saga.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefundEventListener {

    private final PaymentRepository paymentRepository;

    @KafkaListener(
            topics = "${spring.kafka.topic.refundPayment}",
            groupId = "${spring.kafka.group-id}",
            containerFactory = "refundPaymentFactory"
    )
    public void handleRefundPayment(RefundPaymentEvent event, Acknowledgment ack) {
        try {
            log.info("RefundPaymentEvent alındı: {}", event);

            // Gerçek sistemde burada ödeme iade işlemi yapılır (örneğin bir external API çağrılır)
            // Şimdilik sadece loglayalım ve bir refund kaydı yapalım

            Optional<Payment> paymentOpt = paymentRepository.findByOrderId(event.getOrderId());
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
                log.info("Ödeme iade edildi: {}", payment);
            } else {
                log.warn("Refund işlemi için ödeme bulunamadı: orderId={}", event.getOrderId());
            }
            ack.acknowledge();
        }
        catch (Exception e) {
            log.error("RefundPaymentEvent işlenirken hata oluştu: {}", e.getMessage(), e);
        }
    }
}
