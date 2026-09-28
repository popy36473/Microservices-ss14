package com.example.productservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    // Cho phép MỌI USER đã đăng nhập
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.ok(List.of("Laptop", "Phone", "Tablet"));
    }

    // CHỈ CHO PHÉP ADMIN thực hiện
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok("Đã xóa sản phẩm id: " + id + " thành công!");
    }
}