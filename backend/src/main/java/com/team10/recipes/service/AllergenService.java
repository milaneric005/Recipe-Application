package com.team10.recipes.service;

import com.team10.recipes.dto.NameRequest;
import com.team10.recipes.model.Allergen;
import com.team10.recipes.repository.AllergenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AllergenService {

    private final AllergenRepository repository;

    public AllergenService(AllergenRepository repository) {
        this.repository = repository;
    }

    public List<Allergen> getAll() {
        return repository.findAll();
    }

    public Allergen getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Allergen not found"));
    }

    public Allergen create(NameRequest request) {
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Allergen with this name already exists");
        }
        Allergen entity = new Allergen();
        entity.setName(name);
        return repository.save(entity);
    }

    public Allergen update(Integer id, NameRequest request) {
        Allergen entity = getById(id);
        String name = request.name().trim();
        if (!entity.getName().equalsIgnoreCase(name) && repository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Allergen with this name already exists");
        }
        entity.setName(name);
        return repository.save(entity);
    }

    public void delete(Integer id) {
        Allergen entity = getById(id);
        repository.delete(entity);
    }
}
