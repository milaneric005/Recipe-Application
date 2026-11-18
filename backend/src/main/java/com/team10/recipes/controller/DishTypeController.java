package com.team10.recipes.controller;

import com.team10.recipes.model.DishType;
import com.team10.recipes.service.DishTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dish-types")
public class DishTypeController {

    private final DishTypeService service;

    public DishTypeController(DishTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<DishType> getAll() {
        return service.getAll();
    }
}