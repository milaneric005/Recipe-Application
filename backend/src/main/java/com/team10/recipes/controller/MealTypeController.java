package com.team10.recipes.controller;

import com.team10.recipes.dto.NameRequest;
import com.team10.recipes.model.MealType;
import com.team10.recipes.service.MealTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meal-types")
public class MealTypeController {

    private final MealTypeService service;

    public MealTypeController(MealTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<MealType> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public MealType getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MealType create(@Valid @RequestBody NameRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public MealType update(@PathVariable Integer id, @Valid @RequestBody NameRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
