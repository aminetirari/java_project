"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { api, extractErrorMessage } from "@/lib/api";
import type { Order } from "@/lib/types";
import { useAuthStore } from "@/store/authStore";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import OrderStatusBadge from "@/components/OrderStatusBadge";
import OrderTimeline from "@/components/OrderTimeline";

export default function OrderDetailPage({
  params,
}: {
  params: { id: string };
}) {
  const { id } = params;
  const router = useRouter();
  const token = useAuthStore((s) => s.token);
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!token) {
      router.replace("/login");
      return;
    }
    api
      .get<Order>(`/orders/${id}`)
      .then((r) => setOrder(r.data))
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, [id, token, router]);

  const cancel = async () => {
    if (!order) return;
    if (!confirm("Annuler cette commande ? Les stocks seront restaurés.")) return;
    setBusy(true);
    try {
      const { data } = await api.put<Order>(`/orders/${order.id}/cancel`);
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
      <div className="card p-6 text-center">
        Commande introuvable —{" "}
        <Link href="/profile" className="text-indigo-600">
          retour à mon profil
        </Link>
      </div>
    );

  const canCancel = ["PENDING", "PAID", "PAYE"].includes(order.status);
  const canReview = ["PAID", "PAYE", "PROCESSING", "SHIPPED", "DELIVERED"].includes(
    order.status
  );

  return (
    <div className="space-y-6">
      <Link href="/profile" className="text-sm text-slate-500 hover:text-indigo-600">
        ← Retour à mes commandes
      </Link>

      <div className="card flex flex-wrap items-center justify-between gap-3 p-6">
        <div>
          <h1 className="text-2xl font-bold">{order.numeroCommande}</h1>
          <p className="text-sm text-slate-500">
            Passée le {new Date(order.dateCommande).toLocaleString()}
          </p>
        </div>
        <OrderStatusBadge status={order.status} />
      </div>

      <section className="card p-6">
        <h2 className="mb-5 text-sm font-semibold uppercase tracking-wide text-slate-500">
          Suivi de commande
        </h2>
        <OrderTimeline status={order.status} />
      </section>

      <div className="card overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-3">Produit</th>
              <th className="px-4 py-3 text-center">Quantité</th>
              <th className="px-4 py-3 text-right">Prix unitaire</th>
              <th className="px-4 py-3 text-right">Sous-total</th>
            </tr>
          </thead>
          <tbody>
            {order.lignes.map((l) => (
              <tr key={l.id} className="border-t border-slate-200">
                <td className="px-4 py-3">
                  <Link
                    href={`/products/${l.productId}`}
                    className="font-medium text-slate-900 hover:text-indigo-600"
                  >
                    {l.productNom ?? `Produit #${l.productId}`}
                  </Link>
                </td>
                <td className="px-4 py-3 text-center">{l.quantite}</td>
                <td className="px-4 py-3 text-right">
                  {l.prixUnitaire.toFixed(2)} €
                </td>
                <td className="px-4 py-3 text-right font-medium">
                  {l.sousTotal.toFixed(2)} €
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="card space-y-2 p-6 text-sm">
        <div className="flex justify-between">
          <span>Sous-total</span>
          <span>{order.sousTotal.toFixed(2)} €</span>
        </div>
        {order.montantRemise > 0 && (
          <div className="flex justify-between text-emerald-600">
            <span>Remise {order.codePromo ? `(${order.codePromo})` : ""}</span>
            <span>-{order.montantRemise.toFixed(2)} €</span>
          </div>
        )}
        <div className="flex justify-between">
          <span>Livraison</span>
          <span>
            {order.fraisLivraison > 0
              ? `${order.fraisLivraison.toFixed(2)} €`
              : "Offerte"}
          </span>
        </div>
        <div className="flex justify-between border-t border-slate-200 pt-2 text-base font-bold">
          <span>Total</span>
          <span>{order.total.toFixed(2)} €</span>
        </div>
      </div>

      <div className="flex flex-wrap gap-3">
        {canReview && (
          <Link href="/profile/reviews" className="btn-primary">
            Laisser un avis sur ces produits
          </Link>
        )}
        {canCancel && (
          <button type="button" className="btn-danger" disabled={busy} onClick={cancel}>
            {busy ? "Annulation..." : "Annuler la commande"}
          </button>
        )}
      </div>
    </div>
  );
}
