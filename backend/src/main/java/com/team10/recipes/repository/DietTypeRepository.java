package com.team10.recipes.repository;

import com.team10.recipes.model.DietType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DietTypeRepository extends JpaRepository<DietType, Integer> {

    boolean existsByNameIgnoreCase(String name);
}
