package com.team10.recipes.service;

import com.team10.recipes.model.DishType;
import com.team10.recipes.repository.DishTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishTypeService {

    private final DishTypeRepository repository;

    public DishTypeService(DishTypeRepository repository) {
        this.repository = repository;
    }

    public List<DishType> getAll() {
        return repository.findAll();
    }
}