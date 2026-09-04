package com.dapravith.platform.order.domain;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {
  Flux<Order> findAllByCustomerIdOrderByCreatedAtDesc(String customerId);
}
