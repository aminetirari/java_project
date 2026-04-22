"use client";

import { useState, Suspense } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { api, extractErrorMessage } from "@/lib/api";
import type { AuthResponse } from "@/lib/types";
import { useAuthStore } from "@/store/authStore";
import ErrorBox from "@/components/ErrorBox";

function LoginForm() {
  const router = useRouter();
  const search = useSearchParams();
  const redirect = search.get("redirect") || "/";
  const setAuth = useAuthStore((s) => s.setAuth);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const { data } = await api.post<AuthResponse>("/auth/login", {
        email,
        password,
      });
      setAuth({
        token: data.token,
        refreshToken: data.refreshToken,
        user: {
          id: data.id,
          email: data.email,
          prenom: data.prenom ?? "",
          nom: data.nom ?? "",
          role: (data.roles[0]?.replace(/^ROLE_/, "") as never) ?? "CUSTOMER",
        },
        roles: data.roles,
      });
      router.push(redirect);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="mx-auto max-w-md">
      <div className="card space-y-5 p-8">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Bon retour !</h1>
          <p className="mt-1 text-sm text-slate-500">
            Connectez-vous pour continuer vos achats.
          </p>
        </div>
        <ErrorBox message={error ?? undefined} />
        <form onSubmit={onSubmit} className="space-y-3">
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">
              Email
            </label>
            <input
              className="input"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">
              Mot de passe
            </label>
            <input
              className="input"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
            />
          </div>
          <button type="submit" disabled={busy} className="btn-primary w-full">
            {busy ? "Connexion..." : "Se connecter"}
          </button>
        </form>
        <p className="text-center text-sm text-slate-600">
          Pas encore de compte ?{" "}
          <Link href="/register" className="font-medium text-indigo-600 hover:underline">
            Créer un compte
          </Link>
        </p>
        <div className="rounded-lg bg-slate-50 p-3 text-xs text-slate-500">
          <p className="font-semibold">Comptes de démo</p>
          <ul className="mt-1 space-y-0.5">
            <li>admin@shopflow.com / password123</li>
            <li>seller@techstore.com / password123</li>
            <li>jean.dupont@shopflow.com / password123</li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={null}>
      <LoginForm />
    </Suspense>
  );
}
