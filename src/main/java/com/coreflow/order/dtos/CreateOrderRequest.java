package com.coreflow.order.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "Customer ID cannot be null")
        UUID customerId,
        @NotNull(message = "Items cannot be null")
        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid OrderItemDto> items
) { }
