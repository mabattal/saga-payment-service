# Payment Service

Payment Service, bir siparişin ödeme sürecini yönetir. Ödeme sonucu başarılı veya başarısız olabilir. Ayrıca stok yetersizliği durumunda ödeme iadesi (refund) işlemini de gerçekleştirir.


İlişkili repolar:

- [Order Service](https://github.com/mabattal/saga-order-service)
- [Inventory Service](https://github.com/mabattal/saga-inventory-service)

---

## 🧩 Özellikler

- Sipariş için ödeme kontrolü
- Kafka ile `payment-completed` veya `payment-failed` event gönderimi
- Stok yetersizliğinde ödeme iadesi (`refund-payment`)

---

## ⚙️ Konfigürasyon

**application.yml**
```yaml
server:
  port: 8082

spring:
  application:
    name: payment-service

  datasource:
    url: jdbc:postgresql://localhost:5432/sagaPaymentDb
    username: postgres
    password: 1
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  kafka:
    address: ${KAFKA_HOST:localhost}:${KAFKA_PORT:9092}
    group-id: payment-service-group
    topic:
      orderCreated: order-created
      paymentCompleted: payment-completed
      paymentFailed: payment-failed
      paymentRequested: payment-requested
      refundPayment: refund-payment
    consumer:
      enable-auto-commit: false
      auto-offset-reset: earliest
      properties:
        max.poll.records: 5
        spring.json.trusted-packages: '*'

    listener:
      ack-mode: manual
```


---

## 🚀 Kafka Event Akışı

- ⬅️ payment-requested (order-service ➝ payment-service)
- ➡️ payment-completed veya payment-failed
- ⬅️ refund-payment (inventory-service ➝ payment-service)(stok yetersizliği durumunda iade)

## 🛠️ Kullanılan Teknolojiler

- Spring Boot
- Spring Kafka
- PostgreSQL
