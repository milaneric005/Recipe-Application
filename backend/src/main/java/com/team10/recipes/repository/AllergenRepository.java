package com.team10.recipes.repository;

import com.team10.recipes.model.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergenRepository extends JpaRepository<Allergen, Integer> {

    boolean existsByNameIgnoreCase(String name);
}
