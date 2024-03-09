package com.joanjpx.order_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.joanjpx.order_service.model.entities.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}

