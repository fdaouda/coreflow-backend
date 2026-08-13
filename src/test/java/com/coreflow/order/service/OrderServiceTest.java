package com.coreflow.order.service;

import com.coreflow.order.dtos.CreateOrderRequest;
import com.coreflow.order.dtos.OrderItemDto;
import com.coreflow.order.dtos.OrderResponse;
import com.coreflow.order.domain.Order;
import com.coreflow.order.domain.OrderStatus;
import com.coreflow.order.event.OrderProducer;
import com.coreflow.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProducer orderProducer;

    @InjectMocks
    private OrderService orderService;


    @Test
    @DisplayName("Devrait créer une commande avec succès")
    public void createOrder_shouldSaveAndReturnOrder() {
        //given
        UUID customerId = UUID.randomUUID();
        UUID generatedOrderId = UUID.randomUUID();

        CreateOrderRequest createOrderRequest = new CreateOrderRequest(
                customerId, List.of(new OrderItemDto(UUID.randomUUID(), 10, new BigDecimal(1)))
        );

        Order savedOrder = new Order();
        savedOrder.setId(generatedOrderId);
        savedOrder.setCustomerId(customerId);


        // on simule ce que de orderRepository va save et ce qu'il retourne
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);


        //when
        OrderResponse result = orderService.createOrder(createOrderRequest);


        //then
        assertThat(result).isNotNull();

        assertThat(result.orderId()).isEqualTo(generatedOrderId);
        assertThat(result.customerId()).isEqualTo(customerId);

        //on s'assure que la méthode save à été appelé une fois
        verify(orderRepository, times(1)).save(any(Order.class));

    }

    @Test
    @DisplayName("Devrait retourner une commande si l'ID existe")
    public void getOrderById_whenFound_shouldReturnOrder() {
        //given
        UUID customerId = UUID.randomUUID();

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomerId(customerId);
        order.setCreatedAt(Instant.now());
        order.setStatus(OrderStatus.PENDING);
        order.setAmount(new BigDecimal(10));

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        //when
        OrderResponse result = orderService.getOrderById(order.getId());

        //then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(order.getId());

        verify(orderRepository, times(1)).findById(order.getId());

    }

    @Test
    @DisplayName("Devrait lever une exception quand l'id n'existe pas")
    public void getOrderById_whenNotFound_shouldThrowException() {
        //given
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        //when + then
        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Order not found");

    }

    @Test
    @DisplayName("Devrait retourner la liste de toute les commandes")
    public void getAllOrders_shouldReturn_listOfOrders(){
        //given
        Order order1 = new Order();
        order1.setId(UUID.randomUUID());
        order1.setCustomerId(UUID.randomUUID());

        Order order2 = new Order();
        order2.setId(UUID.randomUUID());
        order2.setCustomerId(UUID.randomUUID());

        List<Order> orders = List.of(order1, order2);

        when(orderRepository.findAll()).thenReturn(orders);

        //when
        List<OrderResponse> result = orderService.getAllOrders();

        //then
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.size()).isEqualTo(2);

        verify(orderRepository, times(1)).findAll();
    }

}
