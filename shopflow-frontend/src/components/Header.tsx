'use client';
import Link from 'next/link';
import CartBadge from './CartBadge';
import { useAuthStore } from '@/store/authStore';
import { useEffect, useState } from 'react';

export default function Header() {
  const [mounted, setMounted] = useState(false);
  const { user, logout } = useAuthStore();

  useEffect(() => {
    setMounted(true);
  }, []);

  return (
    <header className="bg-white shadow-sm sticky top-0 z-50">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        <Link href="/" className="text-2xl font-bold text-indigo-600">
          ShopFlow
        </Link>
        <nav className="hidden md:flex gap-6">
          <Link href="/products" className="text-gray-600 hover:text-indigo-600 font-medium">Catalogue</Link>
          <Link href="/cart" className="text-gray-600 hover:text-indigo-600 font-medium flex items-center">
            Panier <CartBadge />
          </Link>
        </nav>
        <div className="flex items-center gap-4">
          {mounted && user ? (
            <div className="flex items-center gap-4">
              {['ADMIN', 'SELLER'].includes(user.role) && (
                <Link href="/dashboard" className="text-sm font-bold bg-indigo-100 text-indigo-700 px-4 py-2 rounded-md hover:bg-indigo-200 transition">
                  Espace Pro
                </Link>
              )}
              <Link href="/profile" className="text-sm font-medium text-gray-700 hover:text-indigo-600 transition">
                Bonjour, {user.nom || user.username || 'Client'}
              </Link>
              <button 
                onClick={() => {
                  logout();
                  window.location.href = '/';
                }}
                className="text-sm font-medium text-red-600 hover:text-red-700 transition"
              >
                Déconnexion
              </button>
            </div>
          ) : (
            <>
              <Link href="/login" className="text-sm font-medium text-gray-700 hover:text-indigo-600">Connexion</Link>
              <Link href="/register" className="text-sm font-medium bg-indigo-600 text-white px-4 py-2 rounded-md hover:bg-indigo-700 transition">S&apos;inscrire</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
