"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api, extractErrorMessage } from "@/lib/api";
import type { Product } from "@/lib/types";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import EmptyState from "@/components/EmptyState";

export default function SellerProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.get<Product[]>("/products/my");
      setProducts(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const remove = async (id: number) => {
    if (!confirm("Supprimer ce produit ?")) return;
    try {
      await api.delete(`/products/${id}`);
      setProducts((p) => p.filter((x) => x.id !== id));
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-3xl font-extrabold text-slate-900">Mes produits</h1>
        <Link href="/dashboard/products/new" className="btn-primary">
          + Nouveau produit
        </Link>
      </div>

      <ErrorBox message={error ?? undefined} />

      {loading ? (
        <Loader />
      ) : products.length === 0 ? (
        <EmptyState
          title="Aucun produit"
          description="Ajoutez votre premier produit pour démarrer vos ventes."
          ctaHref="/dashboard/products/new"
          ctaLabel="Ajouter un produit"
        />
      ) : (
        <div className="card overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
                <th className="px-6 py-3">Produit</th>
                <th className="px-6 py-3">Prix</th>
                <th className="px-6 py-3">Stock</th>
                <th className="px-6 py-3">Statut</th>
                <th className="px-6 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {products.map((p) => (
                <tr key={p.id} className="hover:bg-slate-50">
                  <td className="px-6 py-3 font-medium text-slate-900">{p.nom}</td>
                  <td className="px-6 py-3">{(p.prix ?? 0).toFixed(2)} €</td>
                  <td className="px-6 py-3">{p.stock}</td>
                  <td className="px-6 py-3">
                    <span
                      className={`badge ${
                        p.actif !== false
                          ? "bg-emerald-50 text-emerald-700"
                          : "bg-slate-100 text-slate-600"
                      }`}
                    >
                      {p.actif !== false ? "En ligne" : "Inactif"}
                    </span>
                  </td>
                  <td className="px-6 py-3 text-right">
                    <Link
                      href={`/products/${p.id}`}
                      className="mr-3 text-indigo-600 hover:underline"
                    >
                      Voir
                    </Link>
                    <button
                      type="button"
                      onClick={() => remove(p.id)}
                      className="text-rose-600 hover:underline"
                    >
                      Supprimer
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
