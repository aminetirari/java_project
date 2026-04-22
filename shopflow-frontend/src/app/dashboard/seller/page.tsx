"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api, extractErrorMessage } from "@/lib/api";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import OrderStatusBadge from "@/components/OrderStatusBadge";
import type { Order, Product } from "@/lib/types";

interface SellerDashboard {
  revenus: number;
  revenus30j?: number;
  nbCommandes: number;
  commandesEnAttente: number;
  nbProduits?: number;
  produitsEnRupture?: Product[];
  commandesRecentes?: Order[];
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

  const kpis = [
    {
      label: "Chiffre d'affaires",
      value: `${(data.revenus ?? 0).toFixed(2)} €`,
      accent: "border-l-indigo-500",
    },
    {
      label: "Commandes totales",
      value: data.nbCommandes ?? 0,
      accent: "border-l-emerald-500",
    },
    {
      label: "En attente de traitement",
      value: data.commandesEnAttente ?? 0,
      accent: "border-l-amber-500",
    },
    {
      label: "Produits en catalogue",
      value: data.nbProduits ?? 0,
      accent: "border-l-violet-500",
    },
  ];

  return (
    <div className="space-y-8">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-900">
            Tableau de bord Vendeur
          </h1>
          <p className="mt-1 text-slate-500">
            Suivez vos ventes et gérez votre boutique.
          </p>
        </div>
        <Link href="/dashboard/products/new" className="btn-primary">
          + Nouveau produit
        </Link>
      </header>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {kpis.map((k) => (
          <div key={k.label} className={`card border-l-4 p-5 ${k.accent}`}>
            <p className="text-sm text-slate-500">{k.label}</p>
            <p className="mt-1 text-2xl font-bold text-slate-900">{k.value}</p>
          </div>
        ))}
      </div>

      <section className="grid gap-6 lg:grid-cols-2">
        <div className="card p-5">
          <h2 className="mb-3 text-lg font-semibold">
            Produits en rupture / stock faible
          </h2>
          {(!data.produitsEnRupture || data.produitsEnRupture.length === 0) ? (
            <p className="text-sm text-slate-500">Tous vos stocks sont sains.</p>
          ) : (
            <ul className="divide-y divide-slate-100 text-sm">
              {data.produitsEnRupture.slice(0, 6).map((p) => (
                <li key={p.id} className="flex items-center justify-between py-2">
                  <Link href={`/products/${p.id}`} className="hover:text-indigo-600">
                    {p.nom}
                  </Link>
                  <span
                    className={`badge ${
                      p.stock === 0
                        ? "bg-rose-50 text-rose-700"
                        : "bg-amber-50 text-amber-700"
                    }`}
                  >
                    {p.stock} en stock
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="card p-5">
          <h2 className="mb-3 text-lg font-semibold">Commandes récentes</h2>
          {(!data.commandesRecentes || data.commandesRecentes.length === 0) ? (
            <p className="text-sm text-slate-500">Aucune commande pour l&apos;instant.</p>
          ) : (
            <ul className="divide-y divide-slate-100 text-sm">
              {data.commandesRecentes.slice(0, 6).map((o) => (
                <li key={o.id} className="flex items-center justify-between py-2">
                  <div>
                    <div className="font-medium">{o.numeroCommande}</div>
                    <div className="text-xs text-slate-500">
                      {new Date(o.dateCommande).toLocaleDateString()}
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <OrderStatusBadge status={o.status} />
                    <span className="font-semibold">{o.total.toFixed(2)} €</span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </section>
    </div>
  );
}
