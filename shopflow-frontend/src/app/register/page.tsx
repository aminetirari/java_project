"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { api, extractErrorMessage } from "@/lib/api";
import ErrorBox from "@/components/ErrorBox";

export default function RegisterPage() {
  const router = useRouter();
  const [prenom, setPrenom] = useState("");
  const [nom, setNom] = useState("");
  const [email, setEmail] = useState("");
  const [motDePasse, setMotDePasse] = useState("");
  const [role, setRole] = useState<"CUSTOMER" | "SELLER">("CUSTOMER");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api.post("/auth/register", {
        prenom,
        nom,
        email,
        motDePasse,
        role,
      });
      router.push("/login");
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
          <h1 className="text-2xl font-bold text-slate-900">Créer un compte</h1>
          <p className="mt-1 text-sm text-slate-500">
            Rejoignez ShopFlow en tant que client ou vendeur.
          </p>
        </div>
        <ErrorBox message={error ?? undefined} />
        <form onSubmit={onSubmit} className="space-y-3">
          <div className="grid grid-cols-2 gap-2">
            <input
              className="input"
              placeholder="Prénom"
              value={prenom}
              onChange={(e) => setPrenom(e.target.value)}
              required
            />
            <input
              className="input"
              placeholder="Nom"
              value={nom}
              onChange={(e) => setNom(e.target.value)}
              required
            />
          </div>
          <input
            className="input"
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <input
            className="input"
            type="password"
            placeholder="Mot de passe (min 6 caractères)"
            value={motDePasse}
            onChange={(e) => setMotDePasse(e.target.value)}
            minLength={6}
            required
          />
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">
              Type de compte
            </label>
            <div className="grid grid-cols-2 gap-2">
              {(["CUSTOMER", "SELLER"] as const).map((r) => (
                <button
                  type="button"
                  key={r}
                  onClick={() => setRole(r)}
                  className={`rounded-lg border px-3 py-2 text-sm ${
                    role === r
                      ? "border-indigo-600 bg-indigo-50 text-indigo-700"
                      : "border-slate-300 text-slate-700"
                  }`}
                >
                  {r === "CUSTOMER" ? "Client" : "Vendeur"}
                </button>
              ))}
            </div>
          </div>
          <button type="submit" disabled={busy} className="btn-primary w-full">
            {busy ? "Création..." : "Créer mon compte"}
          </button>
        </form>
        <p className="text-center text-sm text-slate-600">
          Déjà inscrit ?{" "}
          <Link href="/login" className="font-medium text-indigo-600 hover:underline">
            Se connecter
          </Link>
        </p>
      </div>
    </div>
  );
}
