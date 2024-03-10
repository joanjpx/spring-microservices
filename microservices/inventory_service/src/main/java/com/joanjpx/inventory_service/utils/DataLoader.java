package com.joanjpx.inventory_service.utils;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.joanjpx.inventory_service.model.entities.Inventory;
import com.joanjpx.inventory_service.repositories.InventoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner{

    private final InventoryRepository inventoryRepository;

    @SuppressWarnings("null")
    @Override
    public void run(String... args) throws Exception {
        log.info("Loading data....");

        if(inventoryRepository.findAll().size() == 0) {
            inventoryRepository.saveAll(
                List.of(
                    Inventory.builder().sku("iphone_13").quantity(100).build(),
                    Inventory.builder().sku("iphone_13_red").quantity(100).build(),
                    Inventory.builder().sku("iphone_13_blue").quantity(100).build(),
                    Inventory.builder().sku("iphone_14_pro").quantity(100).build()
                )
            );
        }

        log.info("Data loaded....");
    }

}
