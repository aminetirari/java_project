"use client";

import AuthGuard from "@/components/AuthGuard";
import { useAuthStore } from "@/store/authStore";
import Link from "next/link";
import { usePathname } from "next/navigation";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AuthGuard roles={["SELLER", "ADMIN"]}>
      <DashboardShell>{children}</DashboardShell>
    </AuthGuard>
  );
}

function DashboardShell({ children }: { children: React.ReactNode }) {
  const { hasRole } = useAuthStore();
  const pathname = usePathname();

  const isAdmin = hasRole("ADMIN");
  const isSeller = hasRole("SELLER");

  const links: { href: string; label: string; show: boolean }[] = [
    { href: "/dashboard", label: "Vue d'ensemble", show: true },
    { href: "/dashboard/seller", label: "Mon activité vendeur", show: isSeller },
    { href: "/dashboard/products", label: "Mes produits", show: isSeller },
    { href: "/dashboard/orders", label: isAdmin ? "Toutes les commandes" : "Mes commandes", show: isSeller || isAdmin },
    { href: "/dashboard/admin", label: "Administration", show: isAdmin },
    { href: "/dashboard/admin/users", label: "Utilisateurs", show: isAdmin },
    { href: "/dashboard/admin/reviews", label: "Modération avis", show: isAdmin },
  ].filter((l) => l.show);

  return (
    <div className="-mx-4 -my-8 flex min-h-[calc(100vh-140px)] flex-col md:-mx-0 md:flex-row">
      <aside className="w-full bg-slate-900 p-6 text-slate-100 md:w-64">
        <div className="mb-6">
          <h2 className="text-xl font-bold text-white">Espace pro</h2>
          <p className="text-xs uppercase tracking-wide text-slate-400">
            {isAdmin ? "Administrateur" : "Vendeur"}
          </p>
        </div>
        <nav className="flex flex-col gap-1">
          {links.map((l) => {
            const active = pathname === l.href;
            return (
              <Link
                key={l.href}
                href={l.href}
                className={`rounded-lg px-3 py-2 text-sm transition ${
                  active
                    ? "bg-indigo-600 text-white"
                    : "text-slate-200 hover:bg-slate-800"
                }`}
              >
                {l.label}
              </Link>
            );
          })}
          <Link
            href="/"
            className="mt-6 rounded-lg border border-slate-700 px-3 py-2 text-center text-xs text-slate-300 hover:bg-slate-800"
          >
            ← Retour à la boutique
          </Link>
        </nav>
      </aside>
      <main className="flex-1 bg-slate-50 p-6 md:p-10">
        <div className="mx-auto max-w-6xl">{children}</div>
      </main>
    </div>
  );
}
