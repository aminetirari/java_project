"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { api, extractErrorMessage } from "@/lib/api";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import EmptyState from "@/components/EmptyState";
import RatingStars from "@/components/RatingStars";
import type { Review } from "@/lib/types";

export default function AdminReviewsPage() {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);

  const load = () => {
    setLoading(true);
    api
      .get<Review[]>("/reviews/pending")
      .then((r) => setReviews(r.data))
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  async function approve(r: Review) {
    setBusyId(r.id);
    setError(null);
    try {
      await api.put<Review>(`/reviews/${r.id}/approve`);
      setReviews((prev) => prev.filter((x) => x.id !== r.id));
    } catch (e) {
      setError(extractErrorMessage(e));
    } finally {
      setBusyId(null);
    }
  }

  async function reject(r: Review) {
    if (!confirm("Supprimer définitivement cet avis ?")) return;
    setBusyId(r.id);
    setError(null);
    try {
      await api.delete(`/reviews/${r.id}`);
      setReviews((prev) => prev.filter((x) => x.id !== r.id));
    } catch (e) {
      setError(extractErrorMessage(e));
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-3xl font-extrabold text-slate-900">Modération avis</h1>
        <p className="mt-1 text-slate-500">
          Approuvez les avis légitimes et rejetez ceux qui enfreignent les règles de la
          plateforme.
        </p>
      </header>

      <ErrorBox message={error ?? undefined} />

      {loading ? (
        <Loader />
      ) : reviews.length === 0 ? (
        <EmptyState
          title="Tout est à jour"
          description="Aucun avis en attente de modération."
        />
      ) : (
        <ul className="space-y-4">
          {reviews.map((r) => (
            <li
              key={r.id}
              className="card space-y-3 p-5"
              data-review-id={r.id}
            >
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <div className="text-xs uppercase tracking-wide text-slate-400">
                    Avis #{r.id}
                  </div>
                  <div className="mt-1 text-lg font-semibold text-slate-900">
                    {r.productNom ? (
                      <Link
                        href={`/products/${r.productId}`}
                        className="hover:text-indigo-600"
                      >
                        {r.productNom}
                      </Link>
                    ) : (
                      <Link
                        href={`/products/${r.productId}`}
                        className="hover:text-indigo-600"
                      >
                        Produit #{r.productId}
                      </Link>
                    )}
                  </div>
                  <div className="mt-1 text-sm text-slate-500">
                    Par {r.customerName}
                    {r.dateCreation
                      ? ` • ${new Date(r.dateCreation).toLocaleDateString()}`
                      : ""}
                  </div>
                </div>
                <RatingStars value={r.note ?? 0} />
              </div>

              <p className="whitespace-pre-wrap text-sm text-slate-700">
                {r.commentaire}
              </p>

              <div className="flex flex-wrap justify-end gap-2">
                <button
                  type="button"
                  className="btn btn-outline text-xs"
                  onClick={() => reject(r)}
                  disabled={busyId === r.id}
                >
                  {busyId === r.id ? "..." : "Rejeter"}
                </button>
                <button
                  type="button"
                  className="btn btn-primary text-xs"
                  onClick={() => approve(r)}
                  disabled={busyId === r.id}
                >
                  {busyId === r.id ? "..." : "Approuver"}
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
