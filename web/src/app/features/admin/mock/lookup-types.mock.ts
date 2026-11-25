import { LookupGroup, LookupType } from '../models/lookup-type';

const toLookupTypes = (names: string[]): LookupType[] =>
  names.map((name, index) => ({ id: index + 1, name }));

export const LOOKUP_TYPES_MOCK: Record<LookupGroup, LookupType[]> = {
  'dish-types': toLookupTypes([
    'Soup',
    'Stew',
    'Pasta',
    'Pizza',
    'Sandwich',
    'Burger',
    'Risotto',
    'Meat dish',
    'Fish dish',
    'Vegetable dish',
    'Bakery product',
    'Pastry',
    'Cake',
    'Pancakes',
  ]),
  'meal-types': toLookupTypes([
    'Breakfast',
    'Lunch',
    'Dinner',
    'Main dishes',
    'Appetizers',
    'Salads',
    'Desserts',
    'Drinks',
  ]),
  'diet-types': toLookupTypes([
    'Vegetarian',
    'Vegan',
    'Fasting',
    'Non-fasting',
    'Macrobiotic',
    'Pescatarian',
  ]),
  'ingredient-categories': toLookupTypes([
    'Vegetables',
    'Fruit',
    'Meat',
    'Fish',
    'Dairy products',
    'Grains',
    'Spices',
    'Nuts',
  ]),
  allergens: toLookupTypes(['Milk', 'Gluten', 'Peanuts', 'Eggs', 'Soy', 'Tree nuts', 'Fish']),
};
