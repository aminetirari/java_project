"use client";

import { useAuthStore } from "@/store/authStore";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import type { Role } from "@/lib/types";

interface AuthGuardProps {
  children: React.ReactNode;
  roles?: Role[];
  redirectTo?: string;
  fallback?: React.ReactNode;
}

export default function AuthGuard({
  children,
  roles,
  redirectTo = "/login",
  fallback,
}: AuthGuardProps) {
  const { user, hasRole } = useAuthStore();
  const router = useRouter();
  const pathname = usePathname();
  const [mounted, setMounted] = useState(false);
  const [authorized, setAuthorized] = useState(false);

  useEffect(() => {
    setMounted(true);
    if (!user) {
      setAuthorized(false);
      const target = `${redirectTo}?redirect=${encodeURIComponent(pathname ?? "/")}`;
      router.replace(target);
      return;
    }
    if (roles && roles.length > 0 && !roles.some((r) => hasRole(r))) {
      setAuthorized(false);
      router.replace("/");
      return;
    }
    setAuthorized(true);
  }, [user, roles, hasRole, router, pathname, redirectTo]);

  if (!mounted || !authorized) {
    return (
      fallback ?? (
        <div className="p-8 text-center text-slate-500">Chargement…</div>
      )
    );
  }

  return <>{children}</>;
}
