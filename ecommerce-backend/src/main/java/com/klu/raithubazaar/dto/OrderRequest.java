package com.klu.raithubazaar.dto;

import java.util.List;
import java.time.LocalDateTime;

public record OrderRequest(List<OrderLineRequest> items, String deliveryName, String phone, String address,
                           LocalDateTime scheduledFor) {
    public record OrderLineRequest(Long productId, Integer quantity) {}
}
