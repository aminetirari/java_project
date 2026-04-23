"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api, extractErrorMessage } from "@/lib/api";
import type { Order, OrderStatus } from "@/lib/types";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import OrderStatusBadge from "@/components/OrderStatusBadge";

const ALL_STATUSES: OrderStatus[] = [
  "PENDING",
  "PAID",
  "PROCESSING",
  "SHIPPED",
  "DELIVERED",
  "CANCELLED",
];

export default function DashboardOrderDetailPage({
  params,
}: {
  params: { id: string };
}) {
  const orderId = params.id;
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.get<Order>(`/orders/${orderId}`);
      setOrder(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [orderId]);

  const changeStatus = async (status: OrderStatus) => {
    if (!order || status === order.status) return;
    if (status === "CANCELLED") {
      if (!confirm("Annuler cette commande ? Les stocks seront restaurés.")) return;
    }
    setBusy(true);
    try {
      const { data } =
        status === "CANCELLED"
          ? await api.put<Order>(`/orders/${order.id}/cancel`)
          : await api.put<Order>(`/orders/${order.id}/status`, { status });
      setOrder(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  if (loading) return <Loader />;
  if (error) return <ErrorBox message={error} />;
  if (!order)
    return (
      <div className="card p-8 text-center">
        <h2 className="text-xl font-semibold">Commande introuvable</h2>
        <Link
          href="/dashboard/orders"
          className="mt-3 inline-block text-indigo-600 hover:underline"
        >
          ← Retour aux commandes
        </Link>
      </div>
    );

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <Link
            href="/dashboard/orders"
            className="text-xs font-semibold text-indigo-600 hover:underline"
          >
            ← Retour aux commandes
          </Link>
          <h1 className="mt-1 text-3xl font-extrabold text-slate-900">
            {order.numeroCommande}
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Passée le {new Date(order.dateCommande).toLocaleDateString()} à{" "}
            {new Date(order.dateCommande).toLocaleTimeString()}
          </p>
        </div>
        <OrderStatusBadge status={order.status} />
      </div>

      <section className="card p-5">
        <h2 className="mb-3 text-lg font-semibold">Mettre à jour le statut</h2>
        <div className="flex flex-wrap gap-2">
          {ALL_STATUSES.map((s) => {
            const active = order.status === s;
            return (
              <button
                key={s}
                type="button"
                onClick={() => changeStatus(s)}
                disabled={busy || active}
                className={`rounded-full border px-3 py-1 text-xs font-semibold transition disabled:opacity-60 ${
                  active
                    ? "border-indigo-600 bg-indigo-600 text-white"
                    : "border-slate-200 bg-white text-slate-700 hover:border-indigo-300"
                }`}
              >
                {s}
              </button>
            );
          })}
        </div>
      </section>

      <section className="card p-5">
        <h2 className="mb-3 text-lg font-semibold">Articles</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
                <th className="px-4 py-2">Produit</th>
                <th className="px-4 py-2">Prix unitaire</th>
                <th className="px-4 py-2">Qté</th>
                <th className="px-4 py-2 text-right">Sous-total</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {order.lignes?.map((l) => (
                <tr key={l.id}>
                  <td className="px-4 py-2">
                    <Link
                      href={`/products/${l.productId}`}
                      className="font-medium text-slate-900 hover:text-indigo-600"
                    >
                      Produit #{l.productId}
                    </Link>
                  </td>
                  <td className="px-4 py-2 tabular-nums">
                    {(l.prixUnitaire ?? 0).toFixed(2)} €
                  </td>
                  <td className="px-4 py-2">{l.quantite}</td>
                  <td className="px-4 py-2 text-right font-semibold tabular-nums">
                    {(l.sousTotal ?? 0).toFixed(2)} €
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="card p-5">
        <h2 className="mb-3 text-lg font-semibold">Récapitulatif</h2>
        <dl className="space-y-1 text-sm">
          <div className="flex justify-between">
            <dt className="text-slate-500">Sous-total</dt>
            <dd className="tabular-nums">{(order.sousTotal ?? 0).toFixed(2)} €</dd>
          </div>
          {order.montantRemise ? (
            <div className="flex justify-between">
              <dt className="text-slate-500">
                Remise{order.codePromo ? ` (${order.codePromo})` : ""}
              </dt>
              <dd className="tabular-nums text-emerald-600">
                − {(order.montantRemise ?? 0).toFixed(2)} €
              </dd>
            </div>
          ) : null}
          <div className="flex justify-between">
            <dt className="text-slate-500">Frais de livraison</dt>
            <dd className="tabular-nums">
              {(order.fraisLivraison ?? 0).toFixed(2)} €
            </dd>
          </div>
          <div className="flex justify-between border-t border-slate-100 pt-2 text-base font-bold text-slate-900">
            <dt>Total TTC</dt>
            <dd className="tabular-nums">{(order.total ?? 0).toFixed(2)} €</dd>
          </div>
        </dl>
      </section>
    </div>
  );
}
