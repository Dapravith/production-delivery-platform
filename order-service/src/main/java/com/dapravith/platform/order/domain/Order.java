package com.dapravith.platform.order.domain;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
@Table("orders")
public record Order(
    @Id UUID id,
    String customerId,
    BigDecimal amount,
    String currency,
    String status,
    Instant createdAt) {}
