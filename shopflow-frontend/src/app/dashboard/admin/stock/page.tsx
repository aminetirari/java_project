"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { api, extractErrorMessage } from "@/lib/api";
import type { Product } from "@/lib/types";
import { useAuthStore } from "@/store/authStore";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import EmptyState from "@/components/EmptyState";

const DEFAULT_THRESHOLD = 5;

export default function AdminLowStockPage() {
  const router = useRouter();
  const { token, hasRole } = useAuthStore();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [threshold, setThreshold] = useState(DEFAULT_THRESHOLD);

  useEffect(() => {
    if (!token) {
      router.replace("/login?redirect=/dashboard/admin/stock");
      return;
    }
    if (!hasRole("ADMIN") && !hasRole("SELLER")) {
      router.replace("/");
      return;
    }
    setLoading(true);
    setError(null);
    api
      .get<Product[]>(`/products/low-stock`, { params: { threshold } })
      .then((r) => setProducts(r.data))
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, [token, hasRole, router, threshold]);

  const stats = useMemo(() => {
    const rupture = products.filter((p) => (p.stock ?? 0) === 0).length;
    const critique = products.filter(
      (p) => (p.stock ?? 0) > 0 && (p.stock ?? 0) <= 2
    ).length;
    const faible = products.filter((p) => (p.stock ?? 0) > 2).length;
    return { rupture, critique, faible };
  }, [products]);

  return (
    <div className="space-y-6">
      <Link
        href="/dashboard/admin"
        className="text-sm text-slate-500 hover:text-indigo-600"
      >
        ← Tableau de bord admin
      </Link>

      <header className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-900">
            Produits en stock faible
          </h1>
          <p className="mt-1 text-slate-500">
            Produits actifs dont le stock est ≤ au seuil. Trié par stock
            croissant.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <label className="text-sm font-medium text-slate-600">Seuil</label>
          <input
            type="number"
            min={0}
            max={100}
            value={threshold}
            onChange={(e) =>
              setThreshold(Math.max(0, Number(e.target.value) || 0))
            }
            className="w-20 rounded-lg border border-slate-200 bg-white px-2 py-1.5 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-100"
          />
        </div>
      </header>

      <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
        <div className="card p-4">
          <p className="text-xs font-medium uppercase tracking-wide text-rose-600">
            En rupture (0)
          </p>
          <p className="mt-1 text-2xl font-bold text-slate-900">
            {stats.rupture}
          </p>
        </div>
        <div className="card p-4">
          <p className="text-xs font-medium uppercase tracking-wide text-amber-600">
            Critique (1–2)
          </p>
          <p className="mt-1 text-2xl font-bold text-slate-900">
            {stats.critique}
          </p>
        </div>
        <div className="card p-4">
          <p className="text-xs font-medium uppercase tracking-wide text-indigo-600">
            Faible (3–{threshold})
          </p>
          <p className="mt-1 text-2xl font-bold text-slate-900">
            {stats.faible}
          </p>
        </div>
      </div>

      <ErrorBox message={error ?? undefined} />

      {loading ? (
        <Loader />
      ) : products.length === 0 ? (
        <EmptyState
          title="Aucune alerte"
          description={`Tous les produits actifs ont un stock supérieur à ${threshold}.`}
          ctaHref="/dashboard/admin"
          ctaLabel="Retour au dashboard"
        />
      ) : (
        <div className="card overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr>
                <th className="px-4 py-3">Produit</th>
                <th className="px-4 py-3">Vendeur</th>
                <th className="px-4 py-3 text-right">Prix</th>
                <th className="px-4 py-3 text-center">Stock</th>
                <th className="px-4 py-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {products.map((p) => {
                const stock = p.stock ?? 0;
                const tone =
                  stock === 0
                    ? "bg-rose-50 text-rose-700 ring-rose-200"
                    : stock <= 2
                    ? "bg-amber-50 text-amber-700 ring-amber-200"
                    : "bg-indigo-50 text-indigo-700 ring-indigo-200";
                return (
                  <tr
                    key={p.id}
                    className="border-t border-slate-200 hover:bg-slate-50"
                  >
                    <td className="px-4 py-3 font-medium text-slate-900">
                      {p.nom}
                    </td>
                    <td className="px-4 py-3 text-slate-600">
                      {p.sellerNom ?? `#${p.sellerId}`}
                    </td>
                    <td className="px-4 py-3 text-right">
                      {(p.prixPromo ?? p.prix).toFixed(2)} €
                    </td>
                    <td className="px-4 py-3 text-center">
                      <span
                        className={`badge ring-1 ${tone} font-semibold`}
                      >
                        {stock}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <Link
                        href={`/products/${p.id}`}
                        className="text-sm font-medium text-indigo-600 hover:underline"
                      >
                        Voir →
                      </Link>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
