'use client';
import { useAuthStore } from '@/store/authStore';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const { user } = useAuthStore();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    if (!user) {
      router.push('/login');
    } else if (user.role !== 'SELLER' && user.role !== 'ADMIN') {
      router.push('/'); // Redirect non-authorized users
    }
  }, [user, router]);

  if (!mounted || !user) return <div className="p-8 text-center">Chargement...</div>;

  return (
    <div className="flex flex-col md:flex-row min-h-screen bg-gray-50">
      {/* Sidebar */}
      <aside className="bg-indigo-900 text-white w-full md:w-64 p-6 shadow-xl">
        <div className="mb-8">
          <h2 className="text-2xl font-bold tracking-tight">ShopFlow</h2>
          <span className="text-indigo-200 text-sm font-medium uppercase tracking-wider">Espace {user.role === 'ADMIN' ? 'Admin' : 'Vendeur'}</span>
        </div>
        <nav className="space-y-2">
          <Link href="/dashboard" className="block px-4 py-3 rounded-lg bg-indigo-800 hover:bg-indigo-700 transition">
            Vue d'ensemble
          </Link>
          <Link href="/dashboard/products" className="block px-4 py-3 rounded-lg hover:bg-indigo-700 transition">
            Mes Produits
          </Link>
          <Link href="/dashboard/orders" className="block px-4 py-3 rounded-lg hover:bg-indigo-700 transition">
            Commandes clients
          </Link>
          <Link href="/" className="block px-4 py-3 mt-8 rounded-lg border border-indigo-700 hover:bg-indigo-800 transition text-center text-sm">
            Retour à la boutique
          </Link>
        </nav>
      </aside>

      {/* Main Content */}
      <main className="flex-1 p-8">
        <div className="max-w-6xl mx-auto">
          {children}
        </div>
      </main>
    </div>
  );
}