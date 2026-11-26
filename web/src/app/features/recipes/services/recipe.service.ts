import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { RECIPES_MOCK } from '../mock/recipes.mock';
import { Recipe } from '../models/recipe';

@Injectable({ providedIn: 'root' })
export class RecipeService {
  getAll(): Observable<Recipe[]> {
    return of(RECIPES_MOCK);
  }
}