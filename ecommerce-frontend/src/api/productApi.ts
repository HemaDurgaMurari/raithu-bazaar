import type { Product, ProductInput } from "../types/product";
import { api } from "./client";

export async function getProducts(): Promise<Product[]> {
  return (await api.get<Product[]>("/api/products")).data;
}

export async function addProduct(product: ProductInput): Promise<Product> {
  return (await api.post<Product>("/api/products", product)).data;
}

export async function updateProduct(id: number, product: ProductInput): Promise<Product> {
  return (await api.put<Product>(`/api/products/${id}`, product)).data;
}

export async function deleteProduct(id: number): Promise<void> {
  await api.delete(`/api/products/${id}`);
}
