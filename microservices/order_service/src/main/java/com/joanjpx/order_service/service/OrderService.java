package com.joanjpx.order_service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.joanjpx.order_service.model.dtos.BaseResponse;
import com.joanjpx.order_service.model.dtos.OrderItemRequest;
import com.joanjpx.order_service.model.dtos.OrderRequest;
import com.joanjpx.order_service.model.entities.Order;
import com.joanjpx.order_service.model.entities.OrderItem;
import com.joanjpx.order_service.repositories.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClient;

    public void placeOrder(OrderRequest orderRequest) {

        // TO-DO check inventory
        BaseResponse result = this.webClient.build()
            .post()
            .uri("http://localhost:8081/api/inventory")
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
    
            orderRepository.save(order);
        }else{

            throw new IllegalArgumentException("Some of the products are out of stock");
        }


    }

    private OrderItem mapOrderItemRequestToOrderItem(OrderItemRequest orderItemRequest, Order order) {

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setSku(orderItemRequest.getSku());
        orderItem.setPrice(orderItemRequest.getPrice());
        orderItem.setQuantity(orderItemRequest.getQuantity());
        return orderItem;
    }
}
