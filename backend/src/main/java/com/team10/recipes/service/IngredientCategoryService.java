package com.team10.recipes.service;

import com.team10.recipes.dto.NameRequest;
import com.team10.recipes.model.IngredientCategory;
import com.team10.recipes.repository.IngredientCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class IngredientCategoryService {

    private final IngredientCategoryRepository repository;

    public IngredientCategoryService(IngredientCategoryRepository repository) {
        this.repository = repository;
    }

    public List<IngredientCategory> getAll() {
        return repository.findAll();
    }

    public IngredientCategory getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient category not found"));
    }

    public IngredientCategory create(NameRequest request) {
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ingredient category with this name already exists");
        }
        IngredientCategory entity = new IngredientCategory();
        entity.setName(name);
        return repository.save(entity);
    }

    public IngredientCategory update(Integer id, NameRequest request) {
        IngredientCategory entity = getById(id);
        String name = request.name().trim();
        if (!entity.getName().equalsIgnoreCase(name) && repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ingredient category with this name already exists");
        }
        entity.setName(name);
        return repository.save(entity);
    }

    public void delete(Integer id) {
        IngredientCategory entity = getById(id);
        repository.delete(entity);
    }
}
