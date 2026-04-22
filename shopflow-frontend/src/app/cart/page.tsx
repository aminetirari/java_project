"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { api, extractErrorMessage } from "@/lib/api";
import { useAuthStore } from "@/store/authStore";
import { useCartStore } from "@/store/cartStore";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import EmptyState from "@/components/EmptyState";

export default function CartPage() {
  const router = useRouter();
  const token = useAuthStore((s) => s.token);
  const { cart, setCart } = useCartStore();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [coupon, setCoupon] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!token) {
      router.replace("/login?redirect=/cart");
      return;
    }
    api
      .get("/cart")
      .then((r) => setCart(r.data))
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, [token, router, setCart]);

  const updateQty = async (itemId: number, quantite: number) => {
    setBusy(true);
    setError(null);
    try {
      const { data } = await api.put(`/cart/items/${itemId}?quantite=${quantite}`);
      setCart(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const removeItem = async (itemId: number) => {
    setBusy(true);
    setError(null);
    try {
      const { data } = await api.delete(`/cart/items/${itemId}`);
      setCart(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const applyCoupon = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!coupon.trim()) return;
    setBusy(true);
    setError(null);
    try {
      const { data } = await api.post("/cart/coupon", { code: coupon.trim() });
      setCart(data);
      setCoupon("");
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const removeCoupon = async () => {
    setBusy(true);
    try {
      const { data } = await api.delete("/cart/coupon");
      setCart(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  if (loading) return <Loader />;

  if (!cart || cart.lignes.length === 0) {
    return (
      <div className="flex flex-col gap-4">
        <h1 className="section-title">Mon panier</h1>
        <ErrorBox message={error ?? undefined} />
        <EmptyState
          title="Votre panier est vide"
          description="Ajoutez vos premiers produits pour commencer."
          ctaHref="/products"
          ctaLabel="Voir le catalogue"
        />
      </div>
    );
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
      <section className="space-y-4">
        <h1 className="section-title">Mon panier</h1>
        <ErrorBox message={error ?? undefined} />
        <div className="card divide-y divide-slate-200">
          {cart.lignes.map((line) => (
            <div key={line.id} className="flex items-center gap-4 p-4">
              <Link
                href={`/products/${line.productId}`}
                className="flex-1 font-medium text-slate-900 hover:text-indigo-600"
              >
                {line.productNom}
              </Link>
              <div className="flex items-center rounded-lg border border-slate-300">
                <button
                  type="button"
                  onClick={() => updateQty(line.id, Math.max(1, line.quantite - 1))}
                  disabled={busy || line.quantite <= 1}
                  className="px-3 py-1 disabled:opacity-40"
                >
                  −
                </button>
                <span className="min-w-8 text-center">{line.quantite}</span>
                <button
                  type="button"
                  onClick={() => updateQty(line.id, line.quantite + 1)}
                  disabled={busy}
                  className="px-3 py-1"
                >
                  +
                </button>
              </div>
              <div className="w-24 text-right font-semibold">
                {line.sousTotal.toFixed(2)} €
              </div>
              <button
                type="button"
                onClick={() => removeItem(line.id)}
                disabled={busy}
                className="text-slate-400 hover:text-rose-600"
                aria-label="Supprimer"
              >
                <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2m2 0v14a2 2 0 01-2 2H8a2 2 0 01-2-2V6" />
                </svg>
              </button>
            </div>
          ))}
        </div>
      </section>

      <aside className="card sticky top-20 h-fit space-y-4 p-5">
        <h2 className="text-lg font-semibold text-slate-900">Récapitulatif</h2>
        <dl className="space-y-2 text-sm text-slate-700">
          <div className="flex justify-between">
            <dt>Sous-total</dt>
            <dd>{cart.sousTotal.toFixed(2)} €</dd>
          </div>
          {cart.remise > 0 && (
            <div className="flex justify-between text-emerald-600">
              <dt>Remise ({cart.codePromo})</dt>
              <dd>-{cart.remise.toFixed(2)} €</dd>
            </div>
          )}
          <div className="flex justify-between border-t border-slate-200 pt-2 text-base font-bold text-slate-900">
            <dt>Total</dt>
            <dd>{cart.totalCart.toFixed(2)} €</dd>
          </div>
        </dl>

        {cart.codePromo ? (
          <button
            type="button"
            onClick={removeCoupon}
            className="btn-outline w-full"
            disabled={busy}
          >
            Retirer le coupon
          </button>
        ) : (
          <form onSubmit={applyCoupon} className="space-y-2">
            <label className="text-xs font-semibold uppercase text-slate-500">
              Code promo
            </label>
            <div className="flex gap-2">
              <input
                className="input"
                placeholder="BIENVENUE10"
                value={coupon}
                onChange={(e) => setCoupon(e.target.value)}
              />
              <button type="submit" disabled={busy} className="btn-primary">
                Appliquer
              </button>
            </div>
          </form>
        )}

        <Link href="/checkout" className="btn-primary w-full">
          Passer commande
        </Link>
      </aside>
    </div>
  );
}
