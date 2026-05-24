// Copyright (c) 2026 Alvin Wilsen Chan Chua
// GitHub: chuaalvw-y
// Licensed under the Alvin Wilsen Chan Chua Proprietary Use-Only License.
// See LICENSE.txt in the project root for full license information.

package com.company.platform.logging.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable String id) {
        log.info("Fetching order id={}", id);
        return orders.find(id);
    }

    @PostMapping
    public Order create(@RequestBody CreateOrderRequest request) {
        log.info("Creating order for customer={}", request.customerId());
        return orders.create(request);
    }

    public record CreateOrderRequest(String customerId, String ssn, String cardNumber, String description) {
    }

    public record Order(String id, String customerId, String description, String status) {
    }
}
