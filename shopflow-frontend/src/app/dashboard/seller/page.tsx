"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api, extractErrorMessage } from "@/lib/api";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import OrderStatusBadge from "@/components/OrderStatusBadge";
import KpiCard from "@/components/KpiCard";
import Sparkline, { SparklinePoint } from "@/components/Sparkline";
import type { Order, Product } from "@/lib/types";

interface TopProduct {
  id: number;
  nom: string;
  quantiteVendue: number;
  revenus: number;
}

interface SellerDashboard {
  revenus: number;
  revenus30j?: number;
  panierMoyen?: number;
  nbCommandes: number;
  commandesEnAttente: number;
  nbProduits?: number;
  nbProduitsStockFaible?: number;
  noteMoyenne?: number;
  nbAvis?: number;
  ventesParJour?: SparklinePoint[];
  topProduits?: TopProduct[];
  produitsEnRupture?: Product[];
  commandesRecentes?: Order[];
}

function stockTone(stock: number) {
  if (stock === 0) return "bg-rose-100 text-rose-700 border-rose-200";
  if (stock <= 2) return "bg-amber-100 text-amber-700 border-amber-200";
  return "bg-slate-100 text-slate-700 border-slate-200";
}

export default function SellerDashboardPage() {
  const [data, setData] = useState<SellerDashboard | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .get<SellerDashboard>("/dashboard/seller")
      .then((r) => setData(r.data))
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loader />;
  if (error) return <ErrorBox message={error} />;
  if (!data) return null;

  const series = data.ventesParJour ?? [];

  return (
    <div className="space-y-8">
      <header className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-900">
            Tableau de bord Vendeur
          </h1>
          <p className="mt-1 text-slate-500">
            Suivez vos ventes et gérez votre boutique.
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Link href="/dashboard/products/new" className="btn-primary">
            + Nouveau produit
          </Link>
          <Link href="/dashboard/products" className="btn-secondary">
            Mon catalogue
          </Link>
          <Link href="/dashboard/orders" className="btn-secondary">
            Mes commandes
          </Link>
        </div>
      </header>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard
          label="Chiffre d'affaires"
          value={`${(data.revenus ?? 0).toFixed(2)} €`}
          hint={`Panier moyen : ${(data.panierMoyen ?? 0).toFixed(2)} €`}
          accent="border-l-indigo-500"
        />
        <KpiCard
          label="CA — 30 derniers jours"
          value={`${(data.revenus30j ?? 0).toFixed(2)} €`}
          hint={`${data.nbCommandes ?? 0} commande${(data.nbCommandes ?? 0) > 1 ? "s" : ""} au total`}
          accent="border-l-emerald-500"
        />
        <KpiCard
          label="En attente de traitement"
          value={data.commandesEnAttente ?? 0}
          hint={`${data.nbProduitsStockFaible ?? 0} produit(s) en stock faible`}
          accent="border-l-amber-500"
        />
        <KpiCard
          label="Note moyenne"
          value={
            <span>
              {(data.noteMoyenne ?? 0).toFixed(1)}
              <span className="ml-1 text-base text-amber-500">★</span>
            </span>
          }
          hint={`${data.nbAvis ?? 0} avis · ${data.nbProduits ?? 0} produit(s) actifs`}
          accent="border-l-violet-500"
        />
      </div>

      <section className="card p-5">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold">Ventes — 30 derniers jours</h2>
          <span className="badge bg-slate-100 text-slate-700">
            {series.length} jours
          </span>
        </div>
        {series.length ? (
          <Sparkline data={series} color="#059669" height={150} />
        ) : (
          <p className="text-sm text-slate-500">Aucune vente sur la période.</p>
        )}
      </section>

      <section className="grid gap-6 lg:grid-cols-2">
        <div className="card p-5">
          <h2 className="mb-3 text-lg font-semibold">Top 5 produits</h2>
          {(!data.topProduits || data.topProduits.length === 0) ? (
            <p className="text-sm text-slate-500">
              Aucune vente enregistrée pour le moment.
            </p>
          ) : (
            <ul className="divide-y divide-slate-100 text-sm">
              {data.topProduits.map((p, idx) => (
                <li key={p.id} className="flex items-center justify-between py-2">
                  <div className="flex items-center gap-3">
                    <span className="flex h-7 w-7 items-center justify-center rounded-full bg-emerald-50 text-xs font-bold text-emerald-700">
                      {idx + 1}
                    </span>
                    <div>
                      <Link
                        href={`/products/${p.id}`}
                        className="font-medium text-slate-900 hover:text-indigo-600"
                      >
                        {p.nom}
                      </Link>
                      <div className="text-xs text-slate-500">
                        {p.quantiteVendue} unité
                        {Number(p.quantiteVendue) > 1 ? "s" : ""} vendues
                      </div>
                    </div>
                  </div>
                  <span className="font-semibold tabular-nums">
                    {Number(p.revenus).toFixed(2)} €
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="card p-5">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-lg font-semibold">
              Alertes stock (seuil ≤ 5)
            </h2>
            {(data.nbProduitsStockFaible ?? 0) > 0 && (
              <span className="badge bg-rose-50 text-rose-700">
                {data.nbProduitsStockFaible} à surveiller
              </span>
            )}
          </div>
          {(!data.produitsEnRupture || data.produitsEnRupture.length === 0) ? (
            <p className="text-sm text-slate-500">Tous vos stocks sont sains.</p>
          ) : (
            <ul className="divide-y divide-slate-100 text-sm">
              {data.produitsEnRupture.slice(0, 8).map((p) => (
                <li key={p.id} className="flex items-center justify-between py-2">
                  <Link
                    href={`/dashboard/products/${p.id}`}
                    className="font-medium text-slate-900 hover:text-indigo-600"
                  >
                    {p.nom}
                  </Link>
                  <span
                    className={`rounded-full border px-2.5 py-0.5 text-xs font-semibold ${stockTone(
                      p.stock
                    )}`}
                  >
                    {p.stock === 0 ? "Rupture" : `${p.stock} en stock`}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </section>

      <section className="card p-5">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-lg font-semibold">
            Dernières commandes contenant mes produits
          </h2>
          <Link
            href="/dashboard/orders"
            className="text-xs font-semibold text-indigo-600 hover:underline"
          >
            Tout voir →
          </Link>
        </div>
        {(!data.commandesRecentes || data.commandesRecentes.length === 0) ? (
          <p className="text-sm text-slate-500">
            Aucune commande pour l&apos;instant.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="text-left text-xs uppercase tracking-wide text-slate-500">
                  <th className="py-2 pr-4">N° commande</th>
                  <th className="py-2 pr-4">Date</th>
                  <th className="py-2 pr-4">Statut</th>
                  <th className="py-2 pr-4 text-right">Total</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.commandesRecentes.slice(0, 8).map((o) => (
                  <tr key={o.id}>
                    <td className="py-2 pr-4 font-medium text-slate-900">
                      <Link
                        href={`/dashboard/orders/${o.id}`}
                        className="hover:text-indigo-600"
                      >
                        {o.numeroCommande}
                      </Link>
                    </td>
                    <td className="py-2 pr-4 text-slate-500">
                      {new Date(o.dateCommande).toLocaleDateString()}
                    </td>
                    <td className="py-2 pr-4">
                      <OrderStatusBadge status={o.status} />
                    </td>
                    <td className="py-2 pr-4 text-right font-semibold tabular-nums">
                      {(o.total ?? 0).toFixed(2)} €
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
