import type { Metadata } from "next";
import "./globals.css";
import Header from "@/components/Header";
import Link from "next/link";

export const metadata: Metadata = {
  title: "ShopFlow — Plateforme e-commerce multi-vendeurs",
  description:
    "ShopFlow connecte clients, vendeurs et administrateurs autour d'un catalogue moderne avec panier, coupons et suivi des commandes.",
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="fr">
      <body>
        <Header />
        <main className="mx-auto min-h-[calc(100vh-140px)] max-w-7xl px-4 py-8">
          {children}
        </main>
        <footer className="border-t border-slate-200 bg-white">
          <div className="mx-auto flex max-w-7xl flex-col gap-4 px-4 py-6 text-sm text-slate-500 md:flex-row md:items-center md:justify-between">
            <p>© {new Date().getFullYear()} ShopFlow — Mini-projet e-commerce</p>
            <div className="flex gap-4">
              <Link href="/products" className="hover:text-indigo-600">
                Catalogue
              </Link>
              <Link href="/login" className="hover:text-indigo-600">
                Connexion
              </Link>
              <a
                href="http://localhost:8080/swagger-ui/index.html"
                target="_blank"
                rel="noreferrer"
                className="hover:text-indigo-600"
              >
                API docs
              </a>
            </div>
          </div>
        </footer>
      </body>
    </html>
  );
}
