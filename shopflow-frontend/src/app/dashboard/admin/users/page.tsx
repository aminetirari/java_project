"use client";

import { useEffect, useState } from "react";
import { api, extractErrorMessage } from "@/lib/api";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import EmptyState from "@/components/EmptyState";
import { useAuthStore } from "@/store/authStore";
import type { User } from "@/lib/types";

const roleStyle: Record<string, string> = {
  ADMIN: "bg-indigo-50 text-indigo-700 ring-1 ring-indigo-100",
  SELLER: "bg-emerald-50 text-emerald-700 ring-1 ring-emerald-100",
  CUSTOMER: "bg-slate-100 text-slate-700 ring-1 ring-slate-200",
};

export default function AdminUsersPage() {
  const currentUser = useAuthStore((s) => s.user);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);

  const load = () => {
    setLoading(true);
    api
      .get<User[]>("/users")
      .then((r) => setUsers(r.data))
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  async function toggleActive(u: User) {
    setBusyId(u.id);
    setError(null);
    try {
      const { data } = await api.put<User>(`/users/${u.id}/active`, {
        actif: !(u.actif ?? true),
      });
      setUsers((prev) => prev.map((x) => (x.id === u.id ? data : x)));
    } catch (e) {
      setError(extractErrorMessage(e));
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-3xl font-extrabold text-slate-900">Utilisateurs</h1>
        <p className="mt-1 text-slate-500">
          Gérez les comptes clients, vendeurs et administrateurs de la plateforme.
        </p>
      </header>

      <ErrorBox message={error ?? undefined} />

      {loading ? (
        <Loader />
      ) : users.length === 0 ? (
        <EmptyState
          title="Aucun utilisateur"
          description="Aucun compte n'a été créé pour le moment."
        />
      ) : (
        <div className="card overflow-hidden">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-6 py-3 text-left">Nom</th>
                <th className="px-6 py-3 text-left">Email</th>
                <th className="px-6 py-3 text-left">Rôle</th>
                <th className="px-6 py-3 text-left">Statut</th>
                <th className="px-6 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {users.map((u) => {
                const active = u.actif ?? true;
                const isSelf = currentUser?.id === u.id;
                return (
                  <tr key={u.id} className="hover:bg-slate-50">
                    <td className="px-6 py-3 font-medium text-slate-900">
                      {u.prenom} {u.nom}
                    </td>
                    <td className="px-6 py-3 text-slate-600">{u.email}</td>
                    <td className="px-6 py-3">
                      <span className={`badge ${roleStyle[u.role] ?? ""}`}>
                        {u.role}
                      </span>
                    </td>
                    <td className="px-6 py-3">
                      <span
                        className={`badge ${
                          active
                            ? "bg-emerald-50 text-emerald-700 ring-1 ring-emerald-100"
                            : "bg-rose-50 text-rose-700 ring-1 ring-rose-100"
                        }`}
                      >
                        {active ? "Actif" : "Désactivé"}
                      </span>
                    </td>
                    <td className="px-6 py-3 text-right">
                      {isSelf ? (
                        <span className="text-xs italic text-slate-400">
                          Vous
                        </span>
                      ) : (
                        <button
                          type="button"
                          className={`btn ${
                            active ? "btn-outline" : "btn-primary"
                          } text-xs`}
                          onClick={() => toggleActive(u)}
                          disabled={busyId === u.id}
                        >
                          {busyId === u.id
                            ? "..."
                            : active
                            ? "Désactiver"
                            : "Réactiver"}
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
