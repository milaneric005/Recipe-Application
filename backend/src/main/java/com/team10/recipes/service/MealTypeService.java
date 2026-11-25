package com.team10.recipes.service;

import com.team10.recipes.dto.NameRequest;
import com.team10.recipes.model.MealType;
import com.team10.recipes.repository.MealTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MealTypeService {

    private final MealTypeRepository repository;

    public MealTypeService(MealTypeRepository repository) {
        this.repository = repository;
    }

    public List<MealType> getAll() {
        return repository.findAll();
    }

    public MealType getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal type not found"));
    }

    public MealType create(NameRequest request) {
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Meal type with this name already exists");
        }
        MealType entity = new MealType();
        entity.setName(name);
        return repository.save(entity);
    }

    public MealType update(Integer id, NameRequest request) {
        MealType entity = getById(id);
        String name = request.name().trim();
        if (!entity.getName().equalsIgnoreCase(name) && repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Meal type with this name already exists");
        }
        entity.setName(name);
        return repository.save(entity);
    }

    public void delete(Integer id) {
        MealType entity = getById(id);
        repository.delete(entity);
    }
}
