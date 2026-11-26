import { Recipe } from '../models/recipe';

export const RECIPES_MOCK: Recipe[] = [
  { id: 1, title: 'Chicken soup', dishType: 'Soup', author: 'chef', servings: 4, totalTimeMinutes: 60, story: 'Classic homemade soup.' },
  { id: 2, title: 'Beef stew', dishType: 'Stew', author: 'chef', servings: 6, totalTimeMinutes: 120, story: 'Slow cooked beef with vegetables.' },
  { id: 3, title: 'Pasta carbonara', dishType: 'Pasta', author: 'chef', servings: 2, totalTimeMinutes: 25, story: 'Pasta with eggs and bacon.' },
  { id: 4, title: 'Margherita pizza', dishType: 'Pizza', author: 'chef', servings: 3, totalTimeMinutes: 40, story: 'Simple pizza with tomato and mozzarella.' },
];