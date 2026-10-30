package com.klu.raithubazaar.dto;

import java.math.BigDecimal;
import java.util.List;

public record MealPlanResponse(String dish, int servings, String note, List<Ingredient> ingredients) {
    public record Ingredient(String name, String quantity, String note, List<ProductMatch> products) {}
    public record ProductMatch(Long productId, String productName, String unit, BigDecimal price,
                               Integer stock, String imageUrl) {}
}
