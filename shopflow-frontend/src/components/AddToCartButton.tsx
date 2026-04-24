"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { api, extractErrorMessage } from "@/lib/api";
import { useAuthStore } from "@/store/authStore";
import { useCartStore } from "@/store/cartStore";

interface Props {
  productId: number;
  variantId?: number;
  disabled?: boolean;
  quantity?: number;
}

export default function AddToCartButton({
  productId,
  variantId,
  disabled,
  quantity = 1,
}: Props) {
  const router = useRouter();
  const token = useAuthStore((s) => s.token);
  const setCart = useCartStore((s) => s.setCart);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [done, setDone] = useState(false);

  const onClick = async () => {
    if (!token) {
      router.push("/login");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.post("/cart/items", {
        productId,
        variantId,
        quantite: quantity,
      });
      setCart(data);
      setDone(true);
      setTimeout(() => setDone(false), 1500);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col gap-1">
      <button
        type="button"
        onClick={onClick}
        disabled={disabled || loading}
        className="btn-primary w-full"
      >
        {loading ? "Ajout..." : done ? "Ajouté au panier ✓" : "Ajouter au panier"}
      </button>
      {error && <span className="text-xs text-rose-600">{error}</span>}
    </div>
  );
}
