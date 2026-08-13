package com.coreflow.config.ai;

import com.coreflow.order.dtos.OrderResponse;
import com.coreflow.order.service.OrderService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Cette classe est l'outil pour le LLM.
 *
 */
@Component
public class OrderAiToolsConfig {
    private final OrderService orderService;

    public OrderAiToolsConfig(OrderService orderService) {
        this.orderService = orderService;
    }

    public record OrderStatusResponse(String orderId, String status){}
    public record OrderByCustomerIdResponse(List<String> orderIds){}


    @Tool(name = "getOrderStatusTool", description = "Permet de récupérer le status d'un commande à partir de son identifiant (orderId)")
    public OrderStatusResponse getOrderStatusTool(String orderId) {
        OrderResponse orderResponse = orderService.getOrderById(UUID.fromString(orderId));
        return new OrderStatusResponse(orderResponse.orderId().toString(), orderResponse.status().name());
    }

    @Tool(name = "getOrderByCustomerId", description = "Permet de récupérer la liste des comamndes d'un customer (client) à partir de son identifiant (customerId)")
    public List<UUID> getOrderByCustomerId(UUID customerId)
    {
        return orderService.getOrderByCustomerId(customerId);
    }

}
