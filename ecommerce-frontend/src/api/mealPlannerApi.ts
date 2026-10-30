import { api } from "./client";

export interface SuggestedProduct {
  productId: number;
  productName: string;
  unit: string;
  price: number;
  stock: number;
  imageUrl: string;
}

export interface MealIngredient {
  name: string;
  quantity: string;
  note: string;
  products: SuggestedProduct[];
}

export interface MealPlan {
  dish: string;
  servings: number;
  note: string;
  ingredients: MealIngredient[];
}

export async function suggestMeal(dish: string, servings: number): Promise<MealPlan> {
  return (await api.post<MealPlan>("/api/meal-planner/suggest", { dish, servings })).data;
}
