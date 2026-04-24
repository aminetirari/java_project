import Link from "next/link";
import { API_BASE_URL } from "@/lib/api";
import type { Category, Page, Product } from "@/lib/types";
import ProductCard from "@/components/ProductCard";

export const dynamic = "force-dynamic";

async function fetchJson<T>(path: string): Promise<T | null> {
  try {
    const res = await fetch(`${API_BASE_URL}${path}`, { cache: "no-store" });
    if (!res.ok) return null;
    return (await res.json()) as T;
  } catch {
    return null;
  }
}

export default async function Home() {
  const [productsPage, categories, topSelling] = await Promise.all([
    fetchJson<Page<Product>>("/products?size=8&sort=newest"),
    fetchJson<Category[]>("/categories"),
    fetchJson<Product[]>("/products/top-selling"),
  ]);

  const products = productsPage?.content ?? [];
  const cats = categories ?? [];
  const top = topSelling ?? [];

  return (
    <div className="flex flex-col gap-12">
      <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-indigo-600 via-indigo-500 to-violet-600 px-8 py-16 text-white shadow-xl">
        <div className="max-w-2xl space-y-5">
          <span className="badge bg-white/15 text-white ring-1 ring-white/30">
            Plateforme multi-vendeurs
          </span>
          <h1 className="text-4xl font-extrabold leading-tight md:text-5xl">
            Achetez malin. Vendez facilement. Tout, sur ShopFlow.
          </h1>
          <p className="text-lg text-indigo-100">
            Parcourez des milliers de produits, suivez vos commandes en temps
            réel et profitez de coupons exclusifs.
          </p>
          <div className="flex flex-wrap gap-3 pt-2">
            <Link
              href="/products"
              className="btn bg-white text-indigo-700 hover:bg-slate-100"
            >
              Explorer le catalogue
            </Link>
            <Link
              href="/register"
              className="btn bg-white/10 text-white ring-1 ring-white/40 hover:bg-white/20"
            >
              Créer un compte vendeur
            </Link>
          </div>
        </div>
        <div className="pointer-events-none absolute -right-12 -top-12 h-64 w-64 rounded-full bg-white/10 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-16 -right-24 h-80 w-80 rounded-full bg-violet-400/30 blur-3xl" />
      </section>

      <section
        aria-labelledby="promo-banner"
        className="flex flex-col items-center gap-4 rounded-2xl border border-indigo-200 bg-indigo-50 px-6 py-5 text-slate-900 sm:flex-row sm:justify-between sm:px-8"
      >
        <div className="flex items-center gap-4">
          <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-indigo-600 text-white shadow-sm">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="h-6 w-6"
            >
              <path d="M20.59 13.41 11 22.99l-9-9V3h11l7.59 7.59a2 2 0 0 1 0 2.82Z" />
              <circle cx="7" cy="7" r="1.5" fill="currentColor" stroke="none" />
            </svg>
          </span>
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-indigo-700">
              Offre de bienvenue
            </p>
            <h2 id="promo-banner" className="text-lg font-bold leading-tight sm:text-xl">
              -10 % sur votre première commande avec{" "}
              <span className="font-mono text-indigo-700">BIENVENUE10</span>
            </h2>
            <p className="mt-0.5 text-sm text-slate-600">
              Livraison offerte dès 50 €. Valable sur tout le catalogue.
            </p>
          </div>
        </div>
        <Link
          href="/products?promo=true"
          className="btn bg-indigo-600 text-white hover:bg-indigo-700"
        >
          Voir les promos
        </Link>
      </section>

      {cats.length > 0 && (
        <section>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="section-title">Explorer par catégorie</h2>
          </div>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
            {cats.slice(0, 8).map((c) => (
              <Link
                key={c.id}
                href={`/products?categoryId=${c.id}`}
                className="card flex flex-col gap-1 p-4 transition hover:border-indigo-300 hover:shadow-md"
              >
                <span className="text-sm font-semibold text-slate-900">
                  {c.nom}
                </span>
                {c.description && (
                  <span className="line-clamp-2 text-xs text-slate-500">
                    {c.description}
                  </span>
                )}
              </Link>
            ))}
          </div>
        </section>
      )}

      <section>
        <div className="mb-4 flex items-center justify-between">
          <h2 className="section-title">Nouveautés</h2>
          <Link href="/products" className="text-sm text-indigo-600 hover:underline">
            Tout voir →
          </Link>
        </div>
        {products.length === 0 ? (
          <div className="card p-8 text-center text-slate-500">
            Aucun produit disponible pour le moment.
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
            {products.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        )}
      </section>

      {top.length > 0 && (
        <section>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="section-title">Les meilleures ventes</h2>
          </div>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
            {top.slice(0, 4).map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        </section>
      )}

      <section className="grid gap-4 md:grid-cols-3">
        {[
          {
            t: "Livraison rapide",
            d: "Frais offerts dès 50 €. Expédition sous 48h.",
          },
          {
            t: "Coupons exclusifs",
            d: "Profitez de codes promo comme BIENVENUE10 à votre première commande.",
          },
          {
            t: "Paiement sécurisé",
            d: "Paiements chiffrés via Stripe et gestion JWT.",
          },
        ].map((f) => (
          <div key={f.t} className="card p-5">
            <h3 className="font-semibold text-slate-900">{f.t}</h3>
            <p className="mt-1 text-sm text-slate-500">{f.d}</p>
          </div>
        ))}
      </section>
    </div>
  );
}
