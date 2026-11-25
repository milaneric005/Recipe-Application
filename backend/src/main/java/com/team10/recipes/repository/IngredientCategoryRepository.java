package com.team10.recipes.repository;

import com.team10.recipes.model.IngredientCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientCategoryRepository extends JpaRepository<IngredientCategory, Integer> {

    boolean existsByNameIgnoreCase(String name);
}
