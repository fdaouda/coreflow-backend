package com.coreflow.order.controller;

import com.coreflow.order.dtos.CreateOrderRequest;
import com.coreflow.order.dtos.OrderResponse;
import com.coreflow.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Order Management", description = "Endpoints pour la gestion des commandes")
@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Créer une nouvelle commande", description = "Valide et persiste une commande dans la base de données")
    @ApiResponse(responseCode = "201", description = "Commande créée avec succès")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {
        OrderResponse orderResponse = orderService.createOrder(createOrderRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderResponse.orderId())
                .toUri();

        return ResponseEntity.created(location).body(orderResponse);
    }


    @Operation(summary = "Récupérer commandes", description = "Récupère toute les commandes de la base de données")
    @ApiResponse(responseCode = "200")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }


    @Operation(summary = "Récupérer commandes utilisateur", description = "Récupère toutes les commandes en fonction de l'identifiant d'un utilisateur")
    @ApiResponse(responseCode = "200")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}
