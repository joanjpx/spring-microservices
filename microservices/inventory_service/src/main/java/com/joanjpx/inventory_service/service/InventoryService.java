package com.joanjpx.inventory_service.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.joanjpx.inventory_service.model.dtos.BaseResponse;
import com.joanjpx.inventory_service.model.dtos.OrderItemRequest;
import com.joanjpx.inventory_service.model.entities.Inventory;
import com.joanjpx.inventory_service.repositories.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;


    public Boolean isInStock(String sku) {
        
        var inventory = inventoryRepository.findBySku(sku);

        return inventory.filter(i -> i.getQuantity() > 0).isPresent();
    }


    public BaseResponse areInStock(List<OrderItemRequest> orderItems) {
        
        var errorList = new ArrayList<String>();
        List<String> skus = orderItems.stream().map(OrderItemRequest::getSku).toList();
        List<Inventory> inventoryList = inventoryRepository.findBySkuIn(skus);

        orderItems.forEach(orderItemRequest -> {

            var inventory = inventoryList
                .stream()
                .filter(i -> i.getSku().equals(orderItemRequest.getSku()))
                .findFirst();

            if(inventory.isEmpty() || inventory.get().getQuantity() < orderItemRequest.getQuantity()) {
                errorList.add(orderItemRequest.getSku());
            }
        });

    }
}
