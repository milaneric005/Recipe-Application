package com.team10.recipes.repository;

import com.team10.recipes.model.DishType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishTypeRepository extends JpaRepository<DishType, Integer> {
}