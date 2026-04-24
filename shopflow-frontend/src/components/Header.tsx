"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useAuthStore } from "@/store/authStore";
import { useCartStore } from "@/store/cartStore";
import { api } from "@/lib/api";

export default function Header() {
  const router = useRouter();
  const { user, token, logout, hasRole } = useAuthStore();
  const itemCount = useCartStore((s) => s.itemCount());
  const setCart = useCartStore((s) => s.setCart);
  const [q, setQ] = useState("");
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!token) {
      setCart(null);
      return;
    }
    api
      .get("/cart")
      .then((r) => setCart(r.data))
      .catch(() => {
        /* noop */
      });
  }, [token, setCart]);

  const onSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const query = q.trim();
    router.push(query ? `/products?q=${encodeURIComponent(query)}` : "/products");
  };

  const onLogout = async () => {
    try {
      await api.post("/auth/logout");
    } catch {
      /* noop */
    }
    logout();
    router.push("/");
  };

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/80 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center gap-4 px-4 py-3">
        <Link href="/" className="flex items-center gap-2 text-xl font-bold text-indigo-600">
          <span className="inline-block h-7 w-7 rounded-lg bg-indigo-600" />
          ShopFlow
        </Link>

        <form onSubmit={onSearch} className="hidden flex-1 md:block">
          <div className="relative">
            <input
              type="search"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Rechercher un produit..."
              className="input pl-10"
              aria-label="Rechercher"
            />
            <svg
              className="absolute left-3 top-2.5 h-4 w-4 text-slate-400"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <circle cx="11" cy="11" r="7" />
              <path d="m21 21-4.3-4.3" />
            </svg>
          </div>
        </form>

        <nav className="flex items-center gap-4 text-sm">
          <Link href="/products" className="hidden text-slate-700 hover:text-indigo-600 md:inline">
            Catalogue
          </Link>
          <Link
            href="/cart"
            className="relative inline-flex items-center gap-1 text-slate-700 hover:text-indigo-600"
          >
            <svg
              className="h-5 w-5"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              viewBox="0 0 24 24"
            >
              <path d="M6 2l.75 3M6 5h14l-2 10H8L6 5zm2 15a2 2 0 100 4 2 2 0 000-4zm10 0a2 2 0 100 4 2 2 0 000-4z" />
            </svg>
            <span className="hidden md:inline">Panier</span>
            {mounted && itemCount > 0 && (
              <span className="absolute -right-2 -top-2 flex h-5 min-w-5 items-center justify-center rounded-full bg-indigo-600 px-1.5 text-xs font-bold text-white">
                {itemCount}
              </span>
            )}
          </Link>

          {mounted && token ? (
            <>
              <Link href="/profile" className="text-slate-700 hover:text-indigo-600">
                {user?.prenom || "Profil"}
              </Link>
              {hasRole("SELLER") && (
                <Link
                  href="/dashboard/seller"
                  className="rounded-full bg-emerald-50 px-3 py-1 text-emerald-700 hover:bg-emerald-100"
                >
                  Vendeur
                </Link>
              )}
              {hasRole("ADMIN") && (
                <Link
                  href="/dashboard/admin"
                  className="rounded-full bg-amber-50 px-3 py-1 text-amber-700 hover:bg-amber-100"
                >
                  Admin
                </Link>
              )}
              <button
                onClick={onLogout}
                className="text-slate-600 hover:text-red-600"
                type="button"
              >
                Déconnexion
              </button>
            </>
          ) : mounted ? (
            <>
              <Link href="/login" className="text-slate-700 hover:text-indigo-600">
                Connexion
              </Link>
              <Link href="/register" className="btn-primary">
                Inscription
              </Link>
            </>
          ) : null}
        </nav>
      </div>
    </header>
  );
}
