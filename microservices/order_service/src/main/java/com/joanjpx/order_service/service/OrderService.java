package com.joanjpx.order_service.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.joanjpx.order_service.model.dtos.BaseResponse;
import com.joanjpx.order_service.model.dtos.OrderItemRequest;
import com.joanjpx.order_service.model.dtos.OrderItemResponse;
import com.joanjpx.order_service.model.dtos.OrderRequest;
import com.joanjpx.order_service.model.dtos.OrderResponse;
import com.joanjpx.order_service.model.entities.Order;
import com.joanjpx.order_service.model.entities.OrderItem;
import com.joanjpx.order_service.repositories.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClient;

    public OrderResponse placeOrder(OrderRequest orderRequest) {

        // TO-DO check inventory
        BaseResponse result = this.webClient.build()
            .post()
            .uri("http://localhost:8081/api/inventory/in-stock")
            .bodyValue(orderRequest.getOrderItems())
            .retrieve()
            .bodyToMono(BaseResponse.class)
            .block();

        if (result!=null && !result.hasErrors()) {

            System.out.println("Inventory checked out successfully");
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setOrderItems(
                    orderRequest
                        .getOrderItems()
                        .stream()
                        .map(orderItemRequest -> mapOrderItemRequestToOrderItem (orderItemRequest, order))
                        .toList()
            );
    
            return this.mapToOrderResponse(this.orderRepository.save(order));
        }

        throw new IllegalArgumentException("Some of the products are out of stock");
        
    }

    public List<OrderResponse> getAllOrders() {
        List<Order> orders = this.orderRepository.findAll();

        return orders.stream().map(Order -> mapToOrderResponse(Order)).toList();

    }


    private OrderResponse mapToOrderResponse(Order order) {
        
        return new OrderResponse(
            order.getId(), 
            order.getOrderNumber(), 
            order.getOrderItems().stream().map(this::mapToOrderItemRequest).toList()
        );
    }

    private OrderItemResponse mapToOrderItemRequest(OrderItem orderItem) {
        return new OrderItemResponse(orderItem.getId(), orderItem.getSku(), orderItem.getPrice(), orderItem.getQuantity());
    }

    private OrderItem mapOrderItemRequestToOrderItem(OrderItemRequest orderItemRequest, Order order) {

        return OrderItem.builder()
                .id(orderItemRequest.getId())
                .sku(orderItemRequest.getSku())
                .price(orderItemRequest.getPrice())
                .quantity(orderItemRequest.getQuantity())
                .order(order)
                .build();
    }
}
