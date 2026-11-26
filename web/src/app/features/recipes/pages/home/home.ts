import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Recipe } from '../../models/recipe';
import { RecipeService } from '../../services/recipe.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  private readonly recipeService = inject(RecipeService);

  private readonly recipes = toSignal(this.recipeService.getAll(), {
    initialValue: [] as Recipe[],
  });

  readonly search = signal('');

  readonly filteredRecipes = computed(() => {
    const term = this.search().trim().toLowerCase();
    return this.recipes().filter((recipe) => recipe.title.toLowerCase().includes(term));
  });

  onSearch(event: Event) {
    this.search.set((event.target as HTMLInputElement).value);
  }
}