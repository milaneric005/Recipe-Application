package com.team10.recipes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NameRequest(@NotBlank @Size(max = 100) String name) {
}
