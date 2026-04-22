"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { api, extractErrorMessage } from "@/lib/api";
import type { Category } from "@/lib/types";
import ErrorBox from "@/components/ErrorBox";
import { useAuthStore } from "@/store/authStore";

export default function NewProductPage() {
  const router = useRouter();
  const user = useAuthStore((s) => s.user);

  const [nom, setNom] = useState("");
  const [description, setDescription] = useState("");
  const [prix, setPrix] = useState("");
  const [prixPromo, setPrixPromo] = useState("");
  const [stock, setStock] = useState("10");
  const [imageUrls, setImageUrls] = useState("");
  const [categoryIds, setCategoryIds] = useState<number[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .get<Category[]>("/categories")
      .then((r) => setCategories(r.data))
      .catch(() => {});
  }, []);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.id) {
      setError("Connectez-vous à nouveau pour continuer.");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const payload = {
        nom,
        description,
        prix: parseFloat(prix),
        prixPromo: prixPromo ? parseFloat(prixPromo) : null,
        stock: parseInt(stock, 10),
        sellerId: user.id,
        categoryIds,
        images: imageUrls
          .split("\n")
          .map((s) => s.trim())
          .filter(Boolean),
        variantes: [],
      };
      await api.post("/products", payload);
      router.push("/dashboard/products");
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const toggleCategory = (id: number) => {
    setCategoryIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    );
  };

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-extrabold text-slate-900">
          Ajouter un produit
        </h1>
        <Link
          href="/dashboard/products"
          className="text-sm text-indigo-600 hover:underline"
        >
          ← Retour
        </Link>
      </div>

      <ErrorBox message={error ?? undefined} />

      <form onSubmit={submit} className="card space-y-4 p-6">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">
            Nom du produit *
          </label>
          <input
            className="input"
            value={nom}
            onChange={(e) => setNom(e.target.value)}
            required
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">
            Description
          </label>
          <textarea
            className="input"
            rows={4}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>

        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">
              Prix (€) *
            </label>
            <input
              className="input"
              type="number"
              step="0.01"
              min="0"
              value={prix}
              onChange={(e) => setPrix(e.target.value)}
              required
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">
              Prix promo (€)
            </label>
            <input
              className="input"
              type="number"
              step="0.01"
              min="0"
              value={prixPromo}
              onChange={(e) => setPrixPromo(e.target.value)}
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">
              Stock *
            </label>
            <input
              className="input"
              type="number"
              min="0"
              value={stock}
              onChange={(e) => setStock(e.target.value)}
              required
            />
          </div>
        </div>

        {categories.length > 0 && (
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">
              Catégories
            </label>
            <div className="flex flex-wrap gap-2">
              {categories.map((c) => {
                const active = categoryIds.includes(c.id);
                return (
                  <button
                    key={c.id}
                    type="button"
                    onClick={() => toggleCategory(c.id)}
                    className={`rounded-full border px-3 py-1 text-sm ${
                      active
                        ? "border-indigo-600 bg-indigo-50 text-indigo-700"
                        : "border-slate-300 text-slate-600"
                    }`}
                  >
                    {c.nom}
                  </button>
                );
              })}
            </div>
          </div>
        )}

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">
            Images (une URL par ligne)
          </label>
          <textarea
            className="input"
            rows={3}
            value={imageUrls}
            onChange={(e) => setImageUrls(e.target.value)}
            placeholder="https://images.unsplash.com/photo-..."
          />
        </div>

        <div className="flex justify-end gap-3">
          <Link href="/dashboard/products" className="btn-outline">
            Annuler
          </Link>
          <button type="submit" disabled={busy} className="btn-primary">
            {busy ? "Création..." : "Créer le produit"}
          </button>
        </div>
      </form>
    </div>
  );
}
