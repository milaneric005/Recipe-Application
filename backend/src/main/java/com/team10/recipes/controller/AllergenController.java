package com.team10.recipes.controller;

import com.team10.recipes.dto.NameRequest;
import com.team10.recipes.model.Allergen;
import com.team10.recipes.service.AllergenService;
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
@RequestMapping("/api/allergens")
public class AllergenController {

    private final AllergenService service;

    public AllergenController(AllergenService service) {
        this.service = service;
    }

    @GetMapping
    public List<Allergen> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Allergen getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Allergen create(@Valid @RequestBody NameRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Allergen update(@PathVariable Integer id, @Valid @RequestBody NameRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
