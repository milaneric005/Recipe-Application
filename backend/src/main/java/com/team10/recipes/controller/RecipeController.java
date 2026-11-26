package com.team10.recipes.controller;

import com.team10.recipes.dto.RecipeResponse;
import com.team10.recipes.service.RecipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService service;

    public RecipeController(RecipeService service) {
        this.service = service;
    }

    @GetMapping
    public List<RecipeResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public RecipeResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }
}