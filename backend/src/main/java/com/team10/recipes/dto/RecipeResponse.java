package com.team10.recipes.dto;

import com.team10.recipes.model.Recipe;

public record RecipeResponse(
        Long id,
        String title,
        String dishType,
        String author,
        Short servings,
        Integer totalTimeMinutes,
        String story
) {
    public static RecipeResponse from(Recipe recipe) {
        return new RecipeResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getDishType().getName(),
                recipe.getAuthor().getUsername(),
                recipe.getServings(),
                recipe.getTotalTimeMinutes(),
                recipe.getStory()
        );
    }
}