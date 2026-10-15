export type ProductCategory = "Vegetables" | "Fruits" | "Snacks" | "Dairy";

export interface Product {
  productId: number;
  productName: string;
  category: ProductCategory;
  description: string;
  price: number;
  stock: number;
  unit: string;
  imageUrl: string;
  rating: number;
  status: "AVAILABLE" | "OUT_OF_STOCK";
}

export type ProductInput = Omit<Product, "productId" | "status">;
