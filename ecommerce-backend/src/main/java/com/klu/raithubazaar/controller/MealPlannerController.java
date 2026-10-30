package com.klu.raithubazaar.controller;

import com.klu.raithubazaar.dto.MealPlanRequest;
import com.klu.raithubazaar.dto.MealPlanResponse;
import com.klu.raithubazaar.service.MealPlannerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meal-planner")
public class MealPlannerController {
    private final MealPlannerService service;
    public MealPlannerController(MealPlannerService service) { this.service = service; }

    @PostMapping("/suggest")
    public MealPlanResponse suggest(@RequestBody MealPlanRequest request) { return service.suggest(request); }
}
