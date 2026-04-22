"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";

export default function DashboardIndex() {
  const router = useRouter();
  const hasRole = useAuthStore((s) => s.hasRole);

  useEffect(() => {
    if (hasRole("ADMIN")) router.replace("/dashboard/admin");
    else if (hasRole("SELLER")) router.replace("/dashboard/seller");
    else router.replace("/profile");
  }, [hasRole, router]);

  return null;
}
