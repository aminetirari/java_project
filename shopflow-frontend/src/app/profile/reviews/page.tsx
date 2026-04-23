"use client";

import { useEffect, useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { api, extractErrorMessage } from "@/lib/api";
import type { Review, ReviewableItem } from "@/lib/types";
import { useAuthStore } from "@/store/authStore";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import EmptyState from "@/components/EmptyState";
import RatingStars from "@/components/RatingStars";

interface DraftState {
  note: number;
  commentaire: string;
  busy: boolean;
  error?: string;
}

export default function ProfileReviewsPage() {
  const router = useRouter();
  const token = useAuthStore((s) => s.token);

  const [reviewable, setReviewable] = useState<ReviewableItem[]>([]);
  const [myReviews, setMyReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [drafts, setDrafts] = useState<Record<number, DraftState>>({});

  useEffect(() => {
    if (!token) {
      router.replace("/login?redirect=/profile/reviews");
      return;
    }
    setLoading(true);
    Promise.all([
      api.get<ReviewableItem[]>("/reviews/reviewable"),
      api.get<Review[]>("/reviews/my"),
    ])
      .then(([r, m]) => {
        setReviewable(r.data);
        setMyReviews(m.data);
        const init: Record<number, DraftState> = {};
        r.data.forEach((it) => {
          init[it.productId] = { note: 5, commentaire: "", busy: false };
        });
        setDrafts(init);
      })
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, [token, router]);

  const updateDraft = (productId: number, patch: Partial<DraftState>) => {
    setDrafts((d) => ({ ...d, [productId]: { ...d[productId], ...patch } }));
  };

  const submit = async (item: ReviewableItem) => {
    const draft = drafts[item.productId];
    if (!draft) return;
    if (!draft.commentaire.trim()) {
      updateDraft(item.productId, { error: "Merci d'ajouter un commentaire." });
      return;
    }
    updateDraft(item.productId, { busy: true, error: undefined });
    try {
      const { data: newReview } = await api.post<Review>(
        `/products/${item.productId}/reviews`,
        { note: draft.note, commentaire: draft.commentaire }
      );
      setReviewable((list) => list.filter((x) => x.productId !== item.productId));
      setMyReviews((list) => [newReview, ...list]);
    } catch (err) {
      updateDraft(item.productId, { error: extractErrorMessage(err) });
    } finally {
      updateDraft(item.productId, { busy: false });
    }
  };

  const remove = async (review: Review) => {
    if (!confirm("Supprimer cet avis ?")) return;
    try {
      await api.delete(`/reviews/${review.id}`);
      setMyReviews((list) => list.filter((r) => r.id !== review.id));
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="space-y-8">
      <Link
        href="/profile"
        className="text-sm text-slate-500 hover:text-indigo-600"
      >
        ← Retour à mon profil
      </Link>

      <header>
        <h1 className="section-title">Mes avis</h1>
        <p className="mt-1 text-sm text-slate-500">
          Partagez votre expérience sur les produits que vous avez achetés.
        </p>
      </header>

      <ErrorBox message={error ?? undefined} />

      {loading ? (
        <Loader />
      ) : (
        <>
          <section className="space-y-4">
            <h2 className="text-lg font-semibold text-slate-900">
              À évaluer{" "}
              <span className="ml-2 rounded-full bg-indigo-100 px-2 py-0.5 text-xs font-semibold text-indigo-700">
                {reviewable.length}
              </span>
            </h2>
            {reviewable.length === 0 ? (
              <EmptyState
                title="Tout est noté"
                description="Vous n'avez pas de produit en attente d'évaluation. Revenez après votre prochaine commande !"
                ctaHref="/products"
                ctaLabel="Explorer le catalogue"
              />
            ) : (
              <div className="space-y-4">
                {reviewable.map((item) => {
                  const draft = drafts[item.productId] ?? {
                    note: 5,
                    commentaire: "",
                    busy: false,
                  };
                  return (
                    <div
                      key={item.productId}
                      className="card flex flex-col gap-4 p-5 md:flex-row"
                    >
                      <div className="flex w-full items-start gap-3 md:w-60 md:flex-col">
                        <div className="relative h-20 w-20 flex-shrink-0 overflow-hidden rounded-lg border border-slate-200 bg-white md:h-32 md:w-full">
                          <Image
                            src={
                              item.productImage ??
                              `https://ui-avatars.com/api/?name=${encodeURIComponent(
                                item.productNom
                              )}&background=e0e7ff&color=3730a3&size=256`
                            }
                            alt={item.productNom}
                            fill
                            className="object-cover"
                            unoptimized
                          />
                        </div>
                        <div className="min-w-0">
                          <Link
                            href={`/products/${item.productId}`}
                            className="font-semibold text-slate-900 hover:text-indigo-600"
                          >
                            {item.productNom}
                          </Link>
                          <p className="mt-1 text-xs text-slate-500">
                            Commande{" "}
                            <Link
                              href={`/profile/orders/${item.orderId}`}
                              className="text-indigo-600 hover:underline"
                            >
                              {item.numeroCommande}
                            </Link>{" "}
                            — {new Date(item.dateCommande).toLocaleDateString()}
                          </p>
                        </div>
                      </div>

                      <div className="flex-1 space-y-3">
                        <div>
                          <label className="text-xs font-medium uppercase tracking-wide text-slate-500">
                            Votre note
                          </label>
                          <div className="mt-1">
                            <RatingStars
                              value={draft.note}
                              interactive
                              onChange={(v) =>
                                updateDraft(item.productId, { note: v })
                              }
                            />
                          </div>
                        </div>
                        <div>
                          <label
                            htmlFor={`cmt-${item.productId}`}
                            className="text-xs font-medium uppercase tracking-wide text-slate-500"
                          >
                            Votre commentaire
                          </label>
                          <textarea
                            id={`cmt-${item.productId}`}
                            rows={3}
                            value={draft.commentaire}
                            onChange={(e) =>
                              updateDraft(item.productId, {
                                commentaire: e.target.value,
                              })
                            }
                            placeholder="Qu'avez-vous aimé ? Ce qui pourrait être amélioré ?"
                            className="mt-1 w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-100"
                          />
                        </div>
                        {draft.error && (
                          <p className="text-xs text-rose-600">{draft.error}</p>
                        )}
                        <div className="flex justify-end">
                          <button
                            type="button"
                            onClick={() => submit(item)}
                            disabled={draft.busy}
                            className="btn-primary disabled:opacity-60"
                          >
                            {draft.busy ? "Envoi..." : "Publier mon avis"}
                          </button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </section>

          <section className="space-y-4">
            <h2 className="text-lg font-semibold text-slate-900">
              Avis publiés{" "}
              <span className="ml-2 rounded-full bg-slate-100 px-2 py-0.5 text-xs font-semibold text-slate-600">
                {myReviews.length}
              </span>
            </h2>
            {myReviews.length === 0 ? (
              <p className="text-sm text-slate-500">
                Vous n&apos;avez pas encore publié d&apos;avis.
              </p>
            ) : (
              <div className="card divide-y divide-slate-200">
                {myReviews.map((r) => (
                  <div
                    key={r.id}
                    className="flex flex-col gap-2 px-5 py-4 md:flex-row md:items-start md:justify-between"
                  >
                    <div className="flex-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <Link
                          href={`/products/${r.productId}`}
                          className="font-semibold text-slate-900 hover:text-indigo-600"
                        >
                          {r.productNom ?? `Produit #${r.productId}`}
                        </Link>
                        {!r.approuve && (
                          <span className="badge bg-amber-50 text-amber-700 ring-1 ring-amber-200">
                            En modération
                          </span>
                        )}
                      </div>
                      <div className="mt-1">
                        <RatingStars value={r.note} size="sm" />
                      </div>
                      <p className="mt-2 text-sm text-slate-600">{r.commentaire}</p>
                      {r.dateCreation && (
                        <p className="mt-1 text-xs text-slate-400">
                          Publié le{" "}
                          {new Date(r.dateCreation).toLocaleDateString()}
                        </p>
                      )}
                    </div>
                    <button
                      type="button"
                      onClick={() => remove(r)}
                      className="text-sm text-rose-600 hover:underline md:ml-4"
                    >
                      Supprimer
                    </button>
                  </div>
                ))}
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
}
