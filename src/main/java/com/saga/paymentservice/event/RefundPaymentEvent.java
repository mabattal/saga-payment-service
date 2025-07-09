package com.saga.paymentservice.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundPaymentEvent {
    private Long orderId;
    private Long userId;
    private String reason;
}