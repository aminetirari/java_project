"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api, extractErrorMessage } from "@/lib/api";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import OrderStatusBadge from "@/components/OrderStatusBadge";
import type { Order, Product } from "@/lib/types";

interface AdminDashboard {
  revenusTotaux: number;
  revenus30j: number;
  nbCommandes: number;
  nbCommandesEnCours: number;
  nbUtilisateurs?: number;
  nbProduits?: number;
  topProduits?: Product[];
  dernieresCommandes?: Order[];
}

export default function AdminDashboardPage() {
  const [data, setData] = useState<AdminDashboard | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .get<AdminDashboard>("/dashboard/admin")
      .then((r) => setData(r.data))
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loader />;
  if (error) return <ErrorBox message={error} />;
  if (!data) return null;

  const kpis = [
    {
      label: "Chiffre d'affaires total",
      value: `${(data.revenusTotaux ?? 0).toFixed(2)} €`,
      accent: "border-l-indigo-500",
    },
    {
      label: "CA — 30 derniers jours",
      value: `${(data.revenus30j ?? 0).toFixed(2)} €`,
      accent: "border-l-emerald-500",
    },
    {
      label: "Commandes totales",
      value: data.nbCommandes ?? 0,
      accent: "border-l-violet-500",
    },
    {
      label: "Commandes en cours",
      value: data.nbCommandesEnCours ?? 0,
      accent: "border-l-amber-500",
    },
  ];

  return (
    <div className="space-y-8">
      <header>
        <h1 className="text-3xl font-extrabold text-slate-900">
          Tableau de bord Admin
        </h1>
        <p className="mt-1 text-slate-500">
          Vision globale de l&apos;activité de la plateforme ShopFlow.
        </p>
      </header>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {kpis.map((k) => (
          <div
            key={k.label}
            className={`card border-l-4 p-5 ${k.accent}`}
          >
            <p className="text-sm text-slate-500">{k.label}</p>
            <p className="mt-1 text-2xl font-bold text-slate-900">{k.value}</p>
          </div>
        ))}
      </div>

      <section className="grid gap-6 lg:grid-cols-2">
        <div className="card p-5">
          <h2 className="mb-3 text-lg font-semibold">Top produits</h2>
          {(!data.topProduits || data.topProduits.length === 0) ? (
            <p className="text-sm text-slate-500">Aucune vente enregistrée.</p>
          ) : (
            <ul className="divide-y divide-slate-100 text-sm">
              {data.topProduits.slice(0, 5).map((p) => (
                <li key={p.id} className="flex items-center justify-between py-2">
                  <Link href={`/products/${p.id}`} className="hover:text-indigo-600">
                    {p.nom}
                  </Link>
                  <span className="font-semibold">{p.prix.toFixed(2)} €</span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="card p-5">
          <h2 className="mb-3 text-lg font-semibold">Dernières commandes</h2>
          {(!data.dernieresCommandes || data.dernieresCommandes.length === 0) ? (
            <p className="text-sm text-slate-500">Pas encore de commandes.</p>
          ) : (
            <ul className="divide-y divide-slate-100 text-sm">
              {data.dernieresCommandes.slice(0, 6).map((o) => (
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
