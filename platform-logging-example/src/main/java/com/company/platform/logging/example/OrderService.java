// Copyright (c) 2026 Alvin Wilsen Chan Chua
// GitHub: chuaalvw-y
// Licensed under the Alvin Wilsen Chan Chua Proprietary Use-Only License.
// See LICENSE.txt in the project root for full license information.

package com.company.platform.logging.example;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderController.Order find(String id) {
        log.debug("Looking up order id={}", id);
        return new OrderController.Order(id, "customer-1", "Sample order", "OPEN");
    }

    public OrderController.Order create(OrderController.CreateOrderRequest request) {
        String id = UUID.randomUUID().toString();
        log.info("Persisted order id={} customer={}", id, request.customerId());
        return new OrderController.Order(id, request.customerId(), request.description(), "OPEN");
    }
}
