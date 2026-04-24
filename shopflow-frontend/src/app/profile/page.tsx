"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { api, extractErrorMessage } from "@/lib/api";
import { useAuthStore } from "@/store/authStore";
import type { Order } from "@/lib/types";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import EmptyState from "@/components/EmptyState";
import OrderStatusBadge from "@/components/OrderStatusBadge";

export default function ProfilePage() {
  const router = useRouter();
  const { token, user } = useAuthStore();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) {
      router.replace("/login?redirect=/profile");
      return;
    }
    api
      .get<Order[]>("/orders/my")
      .then((r) => setOrders(r.data))
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, [token, router]);

  return (
    <div className="space-y-6">
      <div className="card flex flex-wrap items-center justify-between gap-4 p-6">
        <div>
          <h1 className="section-title">
            Bonjour, {user?.prenom ?? "client"}
          </h1>
          <p className="text-sm text-slate-500">{user?.email}</p>
        </div>
        <div className="flex items-center gap-3">
          <Link
            href="/profile/reviews"
            className="rounded-lg border border-indigo-200 bg-indigo-50 px-3 py-1.5 text-sm font-medium text-indigo-700 hover:bg-indigo-100"
          >
            Mes avis
          </Link>
          <span className="badge bg-indigo-50 text-indigo-700 ring-1 ring-indigo-100">
            {user?.role ?? ""}
          </span>
        </div>
      </div>

      <section>
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-slate-900">Mes commandes</h2>
          <Link href="/products" className="text-sm text-indigo-600 hover:underline">
            Continuer mes achats →
          </Link>
        </div>
        <ErrorBox message={error ?? undefined} />
        {loading ? (
          <Loader />
        ) : orders.length === 0 ? (
          <EmptyState
            title="Aucune commande"
            description="Passez votre première commande pour voir son historique ici."
            ctaHref="/products"
            ctaLabel="Explorer le catalogue"
          />
        ) : (
          <div className="card divide-y divide-slate-200">
            {orders.map((o) => (
              <Link
                key={o.id}
                href={`/profile/orders/${o.id}`}
                className="flex flex-wrap items-center justify-between gap-3 px-5 py-4 hover:bg-slate-50"
              >
                <div>
                  <div className="font-semibold text-slate-900">
                    {o.numeroCommande}
                  </div>
                  <div className="text-sm text-slate-500">
                    {new Date(o.dateCommande).toLocaleDateString()} •{" "}
                    {o.lignes.length} article{o.lignes.length > 1 ? "s" : ""}
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  <OrderStatusBadge status={o.status} />
                  <span className="font-semibold text-slate-900">
                    {(o.total ?? 0).toFixed(2)} €
                  </span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
