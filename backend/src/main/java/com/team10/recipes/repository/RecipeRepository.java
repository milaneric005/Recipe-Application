package com.team10.recipes.repository;

import com.team10.recipes.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByRemovedFalseOrderByCreatedAtDesc();
}