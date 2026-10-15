import { useQuery } from "@tanstack/react-query";
import { getProducts } from "../api/productApi";

export function useProducts() {
  return useQuery({ queryKey: ["products"], queryFn: getProducts });
}
