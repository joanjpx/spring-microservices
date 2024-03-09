package com.joanjpx.product_service.repositories;

import com.joanjpx.product_service.model.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
