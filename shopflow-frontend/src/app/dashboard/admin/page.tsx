"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api, extractErrorMessage } from "@/lib/api";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import OrderStatusBadge from "@/components/OrderStatusBadge";
import KpiCard from "@/components/KpiCard";
import Sparkline, { SparklinePoint } from "@/components/Sparkline";
import MiniBarChart from "@/components/MiniBarChart";
import type { Order, Product } from "@/lib/types";

interface CategoryRevenue {
  nom: string;
  revenus: number;
}

interface TopSeller {
  id: number;
  nom: string;
  revenus: number;
  nbCommandes: number;
}

interface AdminDashboard {
  revenusTotaux: number;
  revenus30j: number;
  revenus30jPrev?: number;
  variation30j?: number;
  panierMoyen?: number;
  nbCommandes: number;
  nbCommandesEnCours: number;
  nbUtilisateurs?: number;
  nbUtilisateursDesactives?: number;
  nbClients?: number;
  nbVendeurs?: number;
  nbProduits?: number;
  nbProduitsStockFaible?: number;
  avisEnAttente?: number;
  ventesParJour?: SparklinePoint[];
  repartitionCategories?: CategoryRevenue[];
  topVendeurs?: TopSeller[];
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

  const series = data.ventesParJour ?? [];
  const alerts = [
    {
      label: "Avis en attente",
      value: data.avisEnAttente ?? 0,
      tone: "bg-amber-50 text-amber-700",
      href: "/dashboard/admin/reviews",
    },
    {
      label: "Comptes désactivés",
      value: data.nbUtilisateursDesactives ?? 0,
      tone: "bg-rose-50 text-rose-700",
      href: "/dashboard/admin/users",
    },
    {
      label: "Produits en stock faible (≤ 5)",
      value: data.nbProduitsStockFaible ?? 0,
      tone: "bg-indigo-50 text-indigo-700",
      href: "/catalog",
    },
  ];

  return (
    <div className="space-y-8">
      <header className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-900">
            Tableau de bord Admin
          </h1>
          <p className="mt-1 text-slate-500">
            Vision globale de l&apos;activité de la plateforme ShopFlow.
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Link href="/dashboard/admin/users" className="btn-secondary">
            Gérer les utilisateurs
          </Link>
          <Link href="/dashboard/admin/reviews" className="btn-secondary">
            Modération avis
          </Link>
        </div>
      </header>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard
          label="Chiffre d'affaires total"
          value={`${(data.revenusTotaux ?? 0).toFixed(2)} €`}
          hint={`Panier moyen : ${(data.panierMoyen ?? 0).toFixed(2)} €`}
          accent="border-l-indigo-500"
        />
        <KpiCard
          label="CA — 30 derniers jours"
          value={`${(data.revenus30j ?? 0).toFixed(2)} €`}
          hint={`vs 30j précédents : ${(data.revenus30jPrev ?? 0).toFixed(2)} €`}
          trend={data.variation30j ?? null}
          accent="border-l-emerald-500"
        />
        <KpiCard
          label="Commandes"
          value={data.nbCommandes ?? 0}
          hint={`${data.nbCommandesEnCours ?? 0} en cours de traitement`}
          accent="border-l-violet-500"
        />
        <KpiCard
          label="Utilisateurs"
          value={data.nbUtilisateurs ?? 0}
          hint={
            <>
              {data.nbClients ?? 0} clients · {data.nbVendeurs ?? 0} vendeurs
            </>
          }
          accent="border-l-amber-500"
        />
      </div>

      <section className="grid gap-6 lg:grid-cols-3">
        <div className="card p-5 lg:col-span-2">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold">Ventes — 30 derniers jours</h2>
            <span className="badge bg-slate-100 text-slate-700">
              {series.length} jours
            </span>
          </div>
          {series.length ? (
            <Sparkline data={series} color="#4f46e5" height={150} />
          ) : (
            <p className="text-sm text-slate-500">Aucune vente sur la période.</p>
          )}
        </div>

        <div className="card p-5">
          <h2 className="mb-3 text-lg font-semibold">Alertes</h2>
          <ul className="space-y-2">
            {alerts.map((a) => (
              <li key={a.label}>
                <Link
                  href={a.href}
                  className={`flex items-center justify-between rounded-lg px-3 py-2 text-sm font-medium ${a.tone} hover:opacity-90`}
                >
                  <span>{a.label}</span>
                  <span className="tabular-nums text-base font-bold">{a.value}</span>
                </Link>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="grid gap-6 lg:grid-cols-2">
        <div className="card p-5">
          <h2 className="mb-3 text-lg font-semibold">CA par catégorie</h2>
          <MiniBarChart
            data={(data.repartitionCategories ?? []).slice(0, 6).map((c) => ({
              label: c.nom,
              value: Number(c.revenus) || 0,
            }))}
            colorClass="bg-indigo-500"
          />
        </div>

        <div className="card p-5">
          <h2 className="mb-3 text-lg font-semibold">Top 5 vendeurs</h2>
          {(!data.topVendeurs || data.topVendeurs.length === 0) ? (
            <p className="text-sm text-slate-500">Pas encore de ventes.</p>
          ) : (
            <ul className="divide-y divide-slate-100 text-sm">
              {data.topVendeurs.map((s, idx) => (
                <li key={s.id} className="flex items-center justify-between py-2">
                  <div className="flex items-center gap-3">
                    <span className="flex h-7 w-7 items-center justify-center rounded-full bg-indigo-50 text-xs font-bold text-indigo-700">
                      {idx + 1}
                    </span>
                    <div>
                      <div className="font-medium text-slate-900">{s.nom}</div>
                      <div className="text-xs text-slate-500">
                        {s.nbCommandes} commande{s.nbCommandes > 1 ? "s" : ""}
                      </div>
                    </div>
                  </div>
                  <span className="font-semibold tabular-nums">
                    {Number(s.revenus).toFixed(2)} €
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </section>

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
                  <span className="font-semibold">{(p.prix ?? 0).toFixed(2)} €</span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="card p-5">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-lg font-semibold">Dernières commandes</h2>
            <Link
              href="/dashboard/orders"
              className="text-xs font-semibold text-indigo-600 hover:underline"
            >
              Tout voir →
            </Link>
          </div>
          {(!data.dernieresCommandes || data.dernieresCommandes.length === 0) ? (
            <p className="text-sm text-slate-500">Pas encore de commandes.</p>
          ) : (
            <ul className="divide-y divide-slate-100 text-sm">
              {data.dernieresCommandes.slice(0, 6).map((o) => (
                <li key={o.id} className="flex items-center justify-between py-2">
                  <div>
                    <Link
                      href={`/dashboard/orders/${o.id}`}
                      className="font-medium text-slate-900 hover:text-indigo-600"
                    >
                      {o.numeroCommande}
                    </Link>
                    <div className="text-xs text-slate-500">
                      {new Date(o.dateCommande).toLocaleDateString()}
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <OrderStatusBadge status={o.status} />
                    <span className="font-semibold">{(o.total ?? 0).toFixed(2)} €</span>
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
