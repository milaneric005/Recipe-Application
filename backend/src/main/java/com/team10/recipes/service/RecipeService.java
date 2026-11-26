package com.team10.recipes.service;

import com.team10.recipes.dto.RecipeResponse;
import com.team10.recipes.repository.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository repository;

    public RecipeService(RecipeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> getAll() {
        return repository.findByRemovedFalseOrderByCreatedAtDesc()
                .stream()
                .map(RecipeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipeResponse getById(Long id) {
        return repository.findById(id)
                .filter(recipe -> !recipe.isRemoved())
                .map(RecipeResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
    }
}