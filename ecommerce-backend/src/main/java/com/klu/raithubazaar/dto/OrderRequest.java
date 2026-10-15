package com.klu.raithubazaar.dto;

import java.util.List;

public record OrderRequest(List<OrderLineRequest> items, String deliveryName, String phone, String address) {
    public record OrderLineRequest(Long productId, Integer quantity) {}
}
