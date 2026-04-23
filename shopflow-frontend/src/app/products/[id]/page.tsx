"use client";

import { useEffect, useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { api, extractErrorMessage } from "@/lib/api";
import type { Product, Review } from "@/lib/types";
import AddToCartButton from "@/components/AddToCartButton";
import RatingStars from "@/components/RatingStars";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import { useAuthStore } from "@/store/authStore";

export default function ProductDetailPage({
  params,
}: {
  params: { id: string };
}) {
  const productId = Number(params.id);
  const token = useAuthStore((s) => s.token);
  const hasRole = useAuthStore((s) => s.hasRole);

  const [product, setProduct] = useState<Product | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [selectedImage, setSelectedImage] = useState(0);
  const [variantId, setVariantId] = useState<number | undefined>();
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [note, setNote] = useState(5);
  const [commentaire, setCommentaire] = useState("");
  const [reviewBusy, setReviewBusy] = useState(false);
  const [reviewError, setReviewError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      api.get<Product>(`/products/${productId}`),
      api.get<Review[]>(`/reviews/product/${productId}`).catch(() => ({ data: [] as Review[] })),
    ])
      .then(([p, r]) => {
        setProduct(p.data);
        setReviews(r.data);
      })
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, [productId]);

  const submitReview = async (e: React.FormEvent) => {
    e.preventDefault();
    setReviewBusy(true);
    setReviewError(null);
    try {
      await api.post(`/products/${productId}/reviews`, { note, commentaire });
      const { data } = await api.get<Review[]>(`/reviews/product/${productId}`);
      setReviews(data);
      setCommentaire("");
      setNote(5);
    } catch (err) {
      setReviewError(extractErrorMessage(err));
    } finally {
      setReviewBusy(false);
    }
  };

  if (loading) return <Loader />;
  if (error) return <ErrorBox message={error} />;
  if (!product)
    return (
      <div className="card p-8 text-center">
        <h2 className="text-xl font-semibold">Produit introuvable</h2>
        <Link href="/products" className="mt-3 inline-block text-indigo-600 hover:underline">
          ← Retour au catalogue
        </Link>
      </div>
    );

  const images = product.images && product.images.length > 0 ? product.images : [
    `https://ui-avatars.com/api/?name=${encodeURIComponent(product.nom)}&background=e0e7ff&color=3730a3&size=800`,
  ];

  const discounted = product.prixPromo != null && product.prixPromo < product.prix;
  const avgNote = reviews.length
    ? reviews.reduce((s, r) => s + r.note, 0) / reviews.length
    : product.noteMoyenne ?? 0;

  return (
    <div className="flex flex-col gap-10">
      <div className="grid gap-8 md:grid-cols-2">
        <div className="space-y-3">
          <div className="relative aspect-square overflow-hidden rounded-2xl border border-slate-200 bg-white">
            <Image
              src={images[selectedImage]}
              alt={product.nom}
              fill
              sizes="(min-width: 768px) 50vw, 100vw"
              className="object-cover"
              unoptimized
              priority
            />
          </div>
          {images.length > 1 && (
            <div className="flex gap-2 overflow-x-auto">
              {images.map((img, idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => setSelectedImage(idx)}
                  className={`relative h-20 w-20 flex-shrink-0 overflow-hidden rounded-lg border-2 ${
                    idx === selectedImage ? "border-indigo-600" : "border-slate-200"
                  }`}
                >
                  <Image src={img} alt="" fill className="object-cover" unoptimized />
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="flex flex-col gap-4">
          {product.sellerNom && (
            <Link
              href={`/products?sellerId=${product.sellerId}`}
              className="text-sm text-slate-500 hover:text-indigo-600"
            >
              Boutique: {product.sellerNom}
            </Link>
          )}
          <h1 className="text-3xl font-bold text-slate-900">{product.nom}</h1>

          {reviews.length > 0 && <RatingStars value={avgNote} count={reviews.length} />}

          <div className="flex items-baseline gap-3">
            {discounted ? (
              <>
                <span className="text-3xl font-extrabold text-rose-600">
                  {product.prixPromo!.toFixed(2)} €
                </span>
                <span className="text-xl text-slate-400 line-through">
                  {product.prix.toFixed(2)} €
                </span>
              </>
            ) : (
              <span className="text-3xl font-extrabold text-slate-900">
                {product.prix.toFixed(2)} €
              </span>
            )}
          </div>

          <p className="text-sm leading-relaxed text-slate-600">
            {product.description}
          </p>

          {product.variantes && product.variantes.length > 0 && (
            <div>
              <label className="mb-1 block text-sm font-semibold text-slate-700">
                Variante
              </label>
              <div className="flex flex-wrap gap-2">
                {product.variantes.map((v) => (
                  <button
                    key={v.id}
                    type="button"
                    onClick={() => setVariantId(v.id === variantId ? undefined : v.id)}
                    className={`btn-outline ${
                      variantId === v.id ? "border-indigo-600 bg-indigo-50 text-indigo-700" : ""
                    }`}
                  >
                    {v.attribut}: {v.valeur}
                  </button>
                ))}
              </div>
            </div>
          )}

          <div className="flex items-center gap-3">
            <label className="text-sm font-medium text-slate-700">Quantité</label>
            <div className="flex items-center rounded-lg border border-slate-300">
              <button
                type="button"
                className="px-3 py-2 text-lg disabled:opacity-40"
                onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                disabled={quantity <= 1}
              >
                −
              </button>
              <span className="min-w-8 text-center">{quantity}</span>
              <button
                type="button"
                className="px-3 py-2 text-lg"
                onClick={() => setQuantity((q) => q + 1)}
              >
                +
              </button>
            </div>
            <span className="text-sm text-slate-500">{product.stock} en stock</span>
          </div>

          <AddToCartButton
            productId={product.id}
            variantId={variantId}
            quantity={quantity}
            disabled={product.stock <= 0}
          />
        </div>
      </div>

      <section className="space-y-4">
        <h2 className="section-title">Avis clients ({reviews.length})</h2>

        {reviews.length === 0 ? (
          <p className="card p-6 text-center text-slate-500">
            Aucun avis pour l&apos;instant.
          </p>
        ) : (
          <div className="grid gap-3">
            {reviews.map((r) => (
              <div key={r.id} className="card p-4">
                <div className="mb-1 flex items-center justify-between">
                  <span className="font-medium text-slate-900">{r.customerName}</span>
                  <RatingStars value={r.note} size="sm" />
                </div>
                <p className="text-sm text-slate-600">{r.commentaire}</p>
              </div>
            ))}
          </div>
        )}

        {token && hasRole("CUSTOMER") && (
          <form onSubmit={submitReview} className="card space-y-3 p-5">
            <h3 className="font-semibold text-slate-900">Laisser un avis</h3>
            <RatingStars value={note} interactive onChange={setNote} />
            <textarea
              className="input"
              rows={3}
              value={commentaire}
              placeholder="Votre avis sur le produit..."
              onChange={(e) => setCommentaire(e.target.value)}
              required
            />
            <ErrorBox message={reviewError ?? undefined} />
            <button type="submit" className="btn-primary" disabled={reviewBusy}>
              {reviewBusy ? "Envoi..." : "Publier l'avis"}
            </button>
          </form>
        )}
      </section>
    </div>
  );
}
