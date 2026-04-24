"use client";

import { useEffect, useMemo, useState, Suspense } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { api, extractErrorMessage } from "@/lib/api";
import type { Category, Page, Product } from "@/lib/types";
import ProductCard from "@/components/ProductCard";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import EmptyState from "@/components/EmptyState";

function ProductsContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const q = searchParams.get("q") ?? "";
  const categoryId = searchParams.get("categoryId") ?? "";
  const prixMin = searchParams.get("prixMin") ?? "";
  const prixMax = searchParams.get("prixMax") ?? "";
  const promo = searchParams.get("promo") === "true";
  const noteMin = searchParams.get("noteMin") ?? "";
  const sort = searchParams.get("sort") ?? "newest";
  const page = Number(searchParams.get("page") ?? "0");

  const [products, setProducts] = useState<Product[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [localQ, setLocalQ] = useState(q);
  const [localMin, setLocalMin] = useState(prixMin);
  const [localMax, setLocalMax] = useState(prixMax);

  const params = useMemo(() => {
    const p = new URLSearchParams();
    if (q) p.set("q", q);
    if (categoryId) p.set("categoryId", categoryId);
    if (prixMin) p.set("prixMin", prixMin);
    if (prixMax) p.set("prixMax", prixMax);
    if (promo) p.set("promo", "true");
    if (noteMin) p.set("noteMin", noteMin);
    if (sort) p.set("sort", sort);
    p.set("page", String(page));
    p.set("size", "12");
    return p.toString();
  }, [q, categoryId, prixMin, prixMax, promo, noteMin, sort, page]);

  useEffect(() => {
    api.get<Category[]>("/categories").then((r) => setCategories(r.data)).catch(() => {});
  }, []);

  useEffect(() => {
    setLoading(true);
    setError(null);
    api
      .get<Page<Product>>(`/products?${params}`)
      .then((r) => {
        setProducts(r.data.content);
        setTotalPages(r.data.totalPages);
        setTotalElements(r.data.totalElements);
      })
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, [params]);

  const updateFilter = (patch: Record<string, string | null>) => {
    const current = new URLSearchParams(searchParams.toString());
    Object.entries(patch).forEach(([k, v]) => {
      if (v === null || v === "") current.delete(k);
      else current.set(k, v);
    });
    if (!("page" in patch)) current.set("page", "0");
    router.push(`/products?${current.toString()}`);
  };

  const onSubmitFilters = (e: React.FormEvent) => {
    e.preventDefault();
    updateFilter({ q: localQ || null, prixMin: localMin || null, prixMax: localMax || null });
  };

  return (
    <div className="grid gap-6 lg:grid-cols-[280px_1fr]">
      <aside className="card sticky top-20 h-fit space-y-5 p-5">
        <form onSubmit={onSubmitFilters} className="space-y-4">
          <div>
            <label className="mb-1 block text-xs font-semibold uppercase text-slate-500">
              Recherche
            </label>
            <input
              className="input"
              placeholder="Rechercher..."
              value={localQ}
              onChange={(e) => setLocalQ(e.target.value)}
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-semibold uppercase text-slate-500">
              Catégorie
            </label>
            <select
              className="input"
              value={categoryId}
              onChange={(e) => updateFilter({ categoryId: e.target.value || null })}
            >
              <option value="">Toutes les catégories</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.nom}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="mb-1 block text-xs font-semibold uppercase text-slate-500">
                Prix min
              </label>
              <input
                type="number"
                min="0"
                className="input"
                value={localMin}
                onChange={(e) => setLocalMin(e.target.value)}
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-semibold uppercase text-slate-500">
                Prix max
              </label>
              <input
                type="number"
                min="0"
                className="input"
                value={localMax}
                onChange={(e) => setLocalMax(e.target.value)}
              />
            </div>
          </div>

          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input
              type="checkbox"
              checked={promo}
              onChange={(e) => updateFilter({ promo: e.target.checked ? "true" : null })}
              className="h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
            />
            Promotions uniquement
          </label>

          <div>
            <label className="mb-1 block text-xs font-semibold uppercase text-slate-500">
              Note minimum
            </label>
            <div className="flex flex-col gap-1">
              {[
                { v: "", label: "Toutes les notes" },
                { v: "4", label: "4 \u2605 et plus" },
                { v: "3", label: "3 \u2605 et plus" },
                { v: "2", label: "2 \u2605 et plus" },
                { v: "1", label: "1 \u2605 et plus" },
              ].map((opt) => (
                <label
                  key={opt.v || "all"}
                  className="flex cursor-pointer items-center gap-2 text-sm text-slate-700"
                >
                  <input
                    type="radio"
                    name="noteMin"
                    value={opt.v}
                    checked={noteMin === opt.v}
                    onChange={() => updateFilter({ noteMin: opt.v || null })}
                    className="h-4 w-4 border-slate-300 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span>{opt.label}</span>
                </label>
              ))}
            </div>
          </div>

          <button type="submit" className="btn-primary w-full">
            Appliquer
          </button>
          <button
            type="button"
            onClick={() => {
              setLocalQ("");
              setLocalMin("");
              setLocalMax("");
              router.push("/products");
            }}
            className="btn-outline w-full"
          >
            Réinitialiser
          </button>
        </form>
      </aside>

      <section>
        <div className="mb-4 flex flex-col items-start justify-between gap-3 sm:flex-row sm:items-center">
          <h1 className="section-title">
            Catalogue
            <span className="ml-2 text-base font-normal text-slate-500">
              ({totalElements} produit{totalElements > 1 ? "s" : ""})
            </span>
          </h1>
          <select
            className="input max-w-xs"
            value={sort}
            onChange={(e) => updateFilter({ sort: e.target.value })}
          >
            <option value="newest">Plus récents</option>
            <option value="price_asc">Prix croissant</option>
            <option value="price_desc">Prix décroissant</option>
            <option value="name">Nom (A-Z)</option>
          </select>
        </div>

        <ErrorBox message={error ?? undefined} />

        {loading ? (
          <Loader />
        ) : products.length === 0 ? (
          <EmptyState title="Aucun produit trouvé" description="Essayez d'ajuster vos filtres." />
        ) : (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-4">
            {products.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        )}

        {totalPages > 1 && (
          <div className="mt-8 flex items-center justify-center gap-2">
            <button
              disabled={page <= 0}
              onClick={() => updateFilter({ page: String(page - 1) })}
              className="btn-outline"
            >
              Précédent
            </button>
            <span className="text-sm text-slate-600">
              Page {page + 1} / {totalPages}
            </span>
            <button
              disabled={page + 1 >= totalPages}
              onClick={() => updateFilter({ page: String(page + 1) })}
              className="btn-outline"
            >
              Suivant
            </button>
          </div>
        )}
      </section>
    </div>
  );
}

export default function ProductsPage() {
  return (
    <Suspense fallback={null}>
      <ProductsContent />
    </Suspense>
  );
}
