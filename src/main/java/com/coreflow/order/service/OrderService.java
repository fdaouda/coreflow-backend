package com.coreflow.order.service;

import com.coreflow.order.dtos.CreateOrderRequest;
import com.coreflow.order.dtos.OrderResponse;
import com.coreflow.order.domain.Order;
import com.coreflow.order.event.OrderCreatedEvent;
import com.coreflow.order.event.OrderProducer;
import com.coreflow.order.mapper.OrderMapper;
import com.coreflow.order.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    public OrderService(OrderRepository orderRepository, OrderProducer orderProducer) {
        this.orderRepository = orderRepository;
        this.orderProducer = orderProducer;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        Order orderEntity = OrderMapper.toEntity(createOrderRequest);

        //DB persitence
        Order savedOrder = orderRepository.save(orderEntity);

        //OrderCreated event kafka
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getAmount()
        );

        //emit event
        orderProducer.sendOrderCreated(orderCreatedEvent);

        return OrderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));

        return OrderMapper.toResponse(order);
    }

    @Transactional
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderMapper::toResponse)
                .toList();
    }


    @Transactional
    public List<UUID> getOrderByCustomerId(UUID customerId) {
        return orderRepository.findOrderIdsByCustomerId(customerId).stream().toList();
    }
}
