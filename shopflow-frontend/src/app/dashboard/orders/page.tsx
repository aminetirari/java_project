"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { api, extractErrorMessage } from "@/lib/api";
import type { Order, OrderStatus } from "@/lib/types";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import EmptyState from "@/components/EmptyState";
import OrderStatusBadge from "@/components/OrderStatusBadge";
import { useAuthStore } from "@/store/authStore";

const STATUS_OPTIONS: (OrderStatus | "ALL")[] = [
  "ALL",
  "PENDING",
  "PAID",
  "PROCESSING",
  "SHIPPED",
  "DELIVERED",
  "CANCELLED",
];

const NEXT_STATUS: Partial<Record<OrderStatus, OrderStatus>> = {
  PENDING: "PROCESSING",
  PAID: "PROCESSING",
  PAYE: "PROCESSING",
  PROCESSING: "SHIPPED",
  SHIPPED: "DELIVERED",
};

export default function DashboardOrdersPage() {
  const hasRole = useAuthStore((s) => s.hasRole);
  const isAdmin = hasRole("ADMIN");

  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<OrderStatus | "ALL">("ALL");
  const [busyId, setBusyId] = useState<number | null>(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.get<Order[]>("/orders/sales");
      setOrders(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const filtered = useMemo(() => {
    if (filter === "ALL") return orders;
    return orders.filter((o) => o.status === filter);
  }, [orders, filter]);

  const totals = useMemo(() => {
    const totalCA = orders
      .filter((o) => o.status !== "CANCELLED")
      .reduce((s, o) => s + (o.total ?? 0), 0);
    const pending = orders.filter(
      (o) => o.status === "PENDING" || o.status === "PAID" || o.status === "PAYE"
    ).length;
    return { totalCA, pending };
  }, [orders]);

  const advance = async (o: Order) => {
    const next = NEXT_STATUS[o.status];
    if (!next) return;
    setBusyId(o.id);
    try {
      const { data } = await api.put<Order>(`/orders/${o.id}/status`, { status: next });
      setOrders((prev) => prev.map((x) => (x.id === o.id ? data : x)));
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-900">
            {isAdmin ? "Toutes les commandes" : "Mes commandes"}
          </h1>
          <p className="mt-1 text-slate-500">
            {isAdmin
              ? "Vue globale des commandes de la plateforme."
              : "Commandes contenant au moins un de vos produits."}
          </p>
        </div>
        <div className="flex flex-wrap gap-3 text-sm">
          <div className="card px-4 py-2">
            <div className="text-xs text-slate-500">CA encaissé</div>
            <div className="text-lg font-bold text-slate-900 tabular-nums">
              {totals.totalCA.toFixed(2)} €
            </div>
          </div>
          <div className="card px-4 py-2">
            <div className="text-xs text-slate-500">À traiter</div>
            <div className="text-lg font-bold text-amber-600 tabular-nums">
              {totals.pending}
            </div>
          </div>
        </div>
      </header>

      <ErrorBox message={error ?? undefined} />

      <div className="flex flex-wrap gap-2">
        {STATUS_OPTIONS.map((s) => {
          const active = filter === s;
          return (
            <button
              key={s}
              type="button"
              onClick={() => setFilter(s)}
              className={`rounded-full border px-3 py-1 text-xs font-medium transition ${
                active
                  ? "border-indigo-600 bg-indigo-600 text-white"
                  : "border-slate-200 bg-white text-slate-600 hover:border-indigo-300"
              }`}
            >
              {s === "ALL" ? "Toutes" : s}
            </button>
          );
        })}
      </div>

      {loading ? (
        <Loader />
      ) : filtered.length === 0 ? (
        <EmptyState
          title="Aucune commande"
          description={
            filter === "ALL"
              ? "Aucune commande pour l'instant."
              : `Aucune commande avec le statut ${filter}.`
          }
        />
      ) : (
        <div className="card overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
                <th className="px-6 py-3">N° commande</th>
                <th className="px-6 py-3">Date</th>
                <th className="px-6 py-3">Articles</th>
                <th className="px-6 py-3">Statut</th>
                <th className="px-6 py-3 text-right">Total</th>
                <th className="px-6 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((o) => {
                const next = NEXT_STATUS[o.status];
                const nbLignes = o.lignes?.length ?? 0;
                return (
                  <tr key={o.id} className="hover:bg-slate-50">
                    <td className="px-6 py-3 font-medium text-slate-900">
                      <Link
                        href={`/dashboard/orders/${o.id}`}
                        className="hover:text-indigo-600"
                      >
                        {o.numeroCommande}
                      </Link>
                    </td>
                    <td className="px-6 py-3 text-slate-500">
                      {new Date(o.dateCommande).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-3 text-slate-500">
                      {nbLignes} article{nbLignes > 1 ? "s" : ""}
                    </td>
                    <td className="px-6 py-3">
                      <OrderStatusBadge status={o.status} />
                    </td>
                    <td className="px-6 py-3 text-right font-semibold tabular-nums">
                      {(o.total ?? 0).toFixed(2)} €
                    </td>
                    <td className="px-6 py-3 text-right">
                      <Link
                        href={`/dashboard/orders/${o.id}`}
                        className="mr-3 text-indigo-600 hover:underline"
                      >
                        Détails
                      </Link>
                      {next ? (
                        <button
                          type="button"
                          onClick={() => advance(o)}
                          disabled={busyId === o.id}
                          className="rounded-md border border-emerald-200 bg-emerald-50 px-2 py-1 text-xs font-semibold text-emerald-700 hover:bg-emerald-100 disabled:opacity-60"
                        >
                          → {next}
                        </button>
                      ) : (
                        <span className="text-xs text-slate-400">—</span>
                      )}
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
