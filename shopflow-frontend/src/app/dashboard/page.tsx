'use client';
import { useAuthStore } from '@/store/authStore';

export default function DashboardPage() {
  const { user } = useAuthStore();
  
  if (!user) return null;

  return (
    <div>
      <header className="mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Bonjour, {user.nom}!</h1>
        <p className="text-gray-500 mt-2">Voici un résumé de votre activité sur ShopFlow.</p>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white p-6 rounded-xl border border-gray-100 shadow-sm border-l-4 border-l-indigo-500">
          <h3 className="text-lg font-semibold text-gray-600 mb-1">Produits en ligne</h3>
          <p className="text-4xl font-extrabold text-indigo-600">0</p>
        </div>
        <div className="bg-white p-6 rounded-xl border border-gray-100 shadow-sm border-l-4 border-l-green-500">
          <h3 className="text-lg font-semibold text-gray-600 mb-1">Commandes récentes</h3>
          <p className="text-4xl font-extrabold text-green-600">0</p>
        </div>
        <div className="bg-white p-6 rounded-xl border border-gray-100 shadow-sm border-l-4 border-l-yellow-500">
          <h3 className="text-lg font-semibold text-gray-600 mb-1">Chiffre d'affaires</h3>
          <p className="text-4xl font-extrabold text-yellow-600">0,00 €</p>
        </div>
      </div>
      
      <div className="bg-indigo-50 border border-indigo-100 p-6 rounded-xl mb-8">
        <h2 className="text-xl font-bold text-indigo-900 mb-2">Prêt à vendre ?</h2>
        <p className="text-indigo-700 mb-4">Commencez par ajouter votre premier produit dans le catalogue.</p>
        <a href="/dashboard/products/new" className="inline-block bg-indigo-600 text-white font-bold py-2 px-6 rounded-lg hover:bg-indigo-700 transition shadow-sm">
          + Ajouter un produit
        </a>
      </div>
    </div>
  );
}