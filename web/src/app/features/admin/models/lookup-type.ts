export interface LookupType {
  id: number;
  name: string;
}

export type LookupGroup =
  'dish-types' | 'meal-types' | 'diet-types' | 'ingredient-categories' | 'allergens';

export interface LookupGroupOption {
  key: LookupGroup;
  label: string;
}
