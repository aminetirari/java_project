"use client";

import { useState } from "react";
import Link from "next/link";
import { api, extractErrorMessage } from "@/lib/api";
import ErrorBox from "@/components/ErrorBox";

interface ForgotResponse {
  message: string;
  devToken?: string;
}

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<ForgotResponse | null>(null);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const { data } = await api.post<ForgotResponse>("/auth/forgot-password", {
        email,
      });
      setSuccess(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const resetLink = success?.devToken
    ? `/reset-password?token=${success.devToken}`
    : null;

  return (
    <div className="mx-auto max-w-md">
      <div className="card space-y-5 p-8">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            Mot de passe oublié
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Saisissez l&apos;email associé à votre compte. Nous vous enverrons
            un lien pour définir un nouveau mot de passe.
          </p>
        </div>

        <ErrorBox message={error ?? undefined} />

        {success ? (
          <div className="space-y-3 rounded-lg border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800">
            <p>{success.message}</p>
            {resetLink && (
              <div className="space-y-2 rounded-md border border-emerald-300 bg-white p-3 text-slate-700">
                <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
                  Mode dev — pas de SMTP configuré
                </p>
                <p>Utilisez ce lien pour réinitialiser votre mot de passe :</p>
                <Link
                  href={resetLink}
                  className="block break-all rounded bg-indigo-50 px-2 py-1 font-mono text-xs text-indigo-700 hover:bg-indigo-100"
                >
                  {resetLink}
                </Link>
              </div>
            )}
            <p className="text-xs text-emerald-700">
              Le lien est valide pendant 60 minutes.
            </p>
          </div>
        ) : (
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
            <button
              type="submit"
              disabled={busy}
              className="btn-primary w-full"
            >
              {busy ? "Envoi..." : "Envoyer le lien"}
            </button>
          </form>
        )}

        <p className="text-center text-sm text-slate-600">
          <Link
            href="/login"
            className="font-medium text-indigo-600 hover:underline"
          >
            ← Retour à la connexion
          </Link>
        </p>
      </div>
    </div>
  );
}
