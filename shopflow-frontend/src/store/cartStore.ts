import { create } from "zustand";
import type { Cart } from "@/lib/types";

interface CartState {
  cart: Cart | null;
  setCart: (cart: Cart | null) => void;
  itemCount: () => number;
}

export const useCartStore = create<CartState>((set, get) => ({
  cart: null,
  setCart: (cart) => set({ cart }),
  itemCount: () => {
    const cart = get().cart;
    if (!cart) return 0;
    return cart.lignes.reduce((acc, l) => acc + l.quantite, 0);
  },
}));
