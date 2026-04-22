'use client';
import { useCartStore } from '@/store/cartStore';
import Link from 'next/link';
import { useEffect, useState } from 'react';

export default function CartPage() {
  const [mounted, setMounted] = useState(false);
  const { items, removeItem, updateQuantity, clearCart, getTotalPrice } = useCartStore();

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) return <div className="p-8 text-center">Chargement du panier...</div>;

  if (items.length === 0) {
    return (
      <div className="max-w-4xl mx-auto py-16 text-center bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
        <span className="text-6xl mb-4 block">🛒</span>
        <h1 className="text-3xl font-bold text-gray-800 mb-4">Votre panier est vide</h1>
        <p className="text-gray-500 mb-8">Découvrez nos produits et trouvez votre bonheur !</p>
        <Link href="/products" className="bg-indigo-600 text-white px-8 py-3 rounded-md font-bold hover:bg-indigo-700 transition">
          Voir la boutique
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto py-8">
      <h1 className="text-3xl font-extrabold text-gray-900 mb-8">Votre Panier</h1>
      
      <div className="flex flex-col lg:flex-row gap-8">
        <div className="flex-grow">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
            <ul className="divide-y divide-gray-100">
              {items.map((item) => (
                <li key={item.id} className="p-6 flex flex-col sm:flex-row items-center justify-between gap-4 hover:bg-gray-50 transition">
                  <div className="flex items-center gap-4 w-full sm:w-auto">
                    <div className="w-16 h-16 bg-gray-100 rounded-md flex items-center justify-center text-2xl">📦</div>
                    <div>
                      <h3 className="font-bold text-gray-800">{item.name}</h3>
                      <p className="text-indigo-600 font-bold">{item.price.toFixed(2)} €</p>
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-6 w-full sm:w-auto justify-between sm:justify-end">
                    <div className="flex items-center border border-gray-300 rounded-md">
                      <button 
                        className="px-3 py-1 text-gray-600 hover:bg-gray-200 font-bold"
                        onClick={() => updateQuantity(item.id, Math.max(1, item.quantity - 1))}
                      >-</button>
                      <span className="px-4 font-medium">{item.quantity}</span>
                      <button 
                        className="px-3 py-1 text-gray-600 hover:bg-gray-200 font-bold"
                        onClick={() => updateQuantity(item.id, item.quantity + 1)}
                      >+</button>
                    </div>
                    
                    <button 
                      onClick={() => removeItem(item.id)}
                      className="text-red-500 hover:text-red-700 font-medium text-sm"
                    >
                      Supprimer
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <div className="w-full lg:w-96 shrink-0">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 sticky top-24">
            <h2 className="text-xl font-bold text-gray-800 mb-6">Résumé de la commande</h2>
            
            <div className="space-y-3 mb-6 text-gray-600">
              <div className="flex justify-between">
                <span>Sous-total</span>
                <span>{getTotalPrice().toFixed(2)} €</span>
              </div>
              <div className="flex justify-between text-green-600">
                <span>Remises</span>
                <span>0.00 €</span>
              </div>
              <div className="border-t pt-3 mt-3 flex justify-between items-center font-extrabold text-xl text-gray-900">
                <span>Total</span>
                <span>{getTotalPrice().toFixed(2)} €</span>
              </div>
            </div>

            <button 
              className="w-full bg-indigo-600 text-white px-6 py-4 rounded-md font-bold hover:bg-indigo-700 transition"
              onClick={() => window.location.href = "/checkout"}
            >
              Passer la commande
            </button>
            
            <button 
              className="w-full mt-4 text-gray-500 hover:text-gray-800 font-medium text-sm underline"
              onClick={clearCart}
            >
              Vider le panier
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
