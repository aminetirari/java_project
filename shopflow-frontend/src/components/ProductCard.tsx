"use client";

import Link from "next/link";
import Image from "next/image";
import type { Product } from "@/lib/types";
import RatingStars from "./RatingStars";

export default function ProductCard({ product }: { product: Product }) {
  const image =
    product.images?.[0] ||
    `https://ui-avatars.com/api/?name=${encodeURIComponent(product.nom)}&background=e0e7ff&color=3730a3&size=400`;
  const discounted = product.prixPromo != null && product.prixPromo < product.prix;

  return (
    <Link
      href={`/products/${product.id}`}
      className="group flex flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
    >
      <div className="relative aspect-square w-full overflow-hidden bg-slate-50">
        <Image
          src={image}
          alt={product.nom}
          fill
          sizes="(min-width: 1024px) 25vw, (min-width: 640px) 33vw, 50vw"
          className="object-cover transition-transform duration-300 group-hover:scale-105"
          unoptimized
        />
        {discounted && (
          <span className="absolute left-2 top-2 badge bg-rose-600 text-white">
            Promo
          </span>
        )}
        {product.stock <= 0 && (
          <span className="absolute right-2 top-2 badge bg-slate-700 text-white">
            Rupture
          </span>
        )}
      </div>
      <div className="flex flex-1 flex-col gap-1 p-3">
        {product.sellerNom && (
          <span className="text-xs uppercase tracking-wide text-slate-500">
            {product.sellerNom}
          </span>
        )}
        <h3 className="line-clamp-2 text-sm font-semibold text-slate-900">
          {product.nom}
        </h3>
        {typeof product.noteMoyenne === "number" && product.noteMoyenne > 0 && (
          <RatingStars value={product.noteMoyenne} count={product.nbAvis} size="sm" />
        )}
        <div className="mt-auto flex items-baseline gap-2 pt-1">
          {discounted ? (
            <>
              <span className="text-lg font-bold text-rose-600">
                {product.prixPromo!.toFixed(2)} €
              </span>
              <span className="text-sm text-slate-400 line-through">
                {product.prix.toFixed(2)} €
              </span>
            </>
          ) : (
            <span className="text-lg font-bold text-slate-900">
              {product.prix.toFixed(2)} €
            </span>
          )}
        </div>
      </div>
    </Link>
  );
}
