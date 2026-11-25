package com.team10.recipes.service;

import com.team10.recipes.dto.NameRequest;
import com.team10.recipes.model.DietType;
import com.team10.recipes.repository.DietTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DietTypeService {

    private final DietTypeRepository repository;

    public DietTypeService(DietTypeRepository repository) {
        this.repository = repository;
    }

    public List<DietType> getAll() {
        return repository.findAll();
    }

    public DietType getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Diet type not found"));
    }

    public DietType create(NameRequest request) {
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Diet type with this name already exists");
        }
        DietType entity = new DietType();
        entity.setName(name);
        return repository.save(entity);
    }

    public DietType update(Integer id, NameRequest request) {
        DietType entity = getById(id);
        String name = request.name().trim();
        if (!entity.getName().equalsIgnoreCase(name) && repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Diet type with this name already exists");
        }
        entity.setName(name);
        return repository.save(entity);
    }

    public void delete(Integer id) {
        DietType entity = getById(id);
        repository.delete(entity);
    }
}
