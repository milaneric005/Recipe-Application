package com.team10.recipes.repository;

import com.team10.recipes.model.MealType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealTypeRepository extends JpaRepository<MealType, Integer> {

    boolean existsByNameIgnoreCase(String name);
}
