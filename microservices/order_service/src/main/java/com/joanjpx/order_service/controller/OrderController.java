package com.joanjpx.order_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.joanjpx.order_service.model.dtos.OrderRequest;
import com.joanjpx.order_service.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest) {
        
        this.orderService.placeOrder(orderRequest);

        return "Order placed successfully";
    }

    @GetMapping
    public String getAllOrders() {
        

        // return this.orderService.getAllOrders();
        return "Get all orders";


    }
}
