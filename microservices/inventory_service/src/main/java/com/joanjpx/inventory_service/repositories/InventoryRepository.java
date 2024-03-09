package com.joanjpx.inventory_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.joanjpx.inventory_service.model.entities.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findBySku(String sku);
    
    List<Inventory> findBySkuIn(List<String> skus);
}
