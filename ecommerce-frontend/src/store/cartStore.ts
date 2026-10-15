import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { CartItem } from "../types/cart";
import type { Product } from "../types/product";

interface CartState {
  cartItems: CartItem[];
  addToCart: (product: Product) => void;
  increaseQuantity: (id: number) => void;
  decreaseQuantity: (id: number) => void;
  clearCart: () => void;
}

export const useCartStore = create<CartState>()(persist(
  (set) => ({
    cartItems: [],
    addToCart: (product) => set((state) => {
      const existing = state.cartItems.find((item) => item.product.productId === product.productId);
      if (existing) {
        if (existing.quantity >= product.stock) return state;
        return { cartItems: state.cartItems.map((item) => item.product.productId === product.productId
          ? { ...item, quantity: item.quantity + 1, product }
          : item) };
      }
      if (product.stock < 1) return state;
      return { cartItems: [...state.cartItems, { product, quantity: 1 }] };
    }),
    increaseQuantity: (id) => set((state) => ({
      cartItems: state.cartItems.map((item) => item.product.productId === id
        && item.quantity < item.product.stock
        ? { ...item, quantity: item.quantity + 1 }
        : item),
    })),
    decreaseQuantity: (id) => set((state) => ({
      cartItems: state.cartItems.flatMap((item) => {
        if (item.product.productId !== id) return [item];
        return item.quantity <= 1 ? [] : [{ ...item, quantity: item.quantity - 1 }];
      }),
    })),
    clearCart: () => set({ cartItems: [] }),
  }),
  { name: "raithu_cart" },
));
