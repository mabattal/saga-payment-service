package com.saga.paymentservice.config;

import com.saga.paymentservice.event.*;
import com.saga.paymentservice.property.KafkaProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfiguration {

    private final KafkaProperties kafkaProperties;

    private <T> JsonDeserializer<T> createJsonDeserializer(Class<T> targetType) {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType, false);
        deserializer.addTrustedPackages("*");
        return deserializer;
    }

    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> targetType) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getAddress());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getGroupId());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        if (kafkaProperties.getConsumer() != null && kafkaProperties.getConsumer().getProperties() != null) {
            config.putAll(kafkaProperties.getConsumer().getProperties());
        }

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                createJsonDeserializer(targetType)
        );
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createListenerFactory(Class<T> targetType) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createConsumerFactory(targetType));

        if (kafkaProperties.getListener() != null &&
                kafkaProperties.getListener().getAckMode() != null) {
            factory.getContainerProperties().setAckMode(
                    ContainerProperties.AckMode.valueOf(
                            kafkaProperties.getListener().getAckMode().toUpperCase()
                    )
            );
        }

        return factory;
    }

    // === Topic'e özel factory bean'leri ===

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentRequestedEvent> paymentRequestedFactory() {
        return createListenerFactory(PaymentRequestedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> paymentCompletedFactory() {
        return createListenerFactory(PaymentCompletedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> paymentFailedFactory() {
        return createListenerFactory(PaymentFailedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderCreatedFactory() {
        return createListenerFactory(OrderCreatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RefundPaymentEvent> refundPaymentFactory() {
        return createListenerFactory(RefundPaymentEvent.class);
    }
}
