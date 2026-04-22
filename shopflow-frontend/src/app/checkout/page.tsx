'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useCartStore } from '@/store/cartStore';
import { useAuthStore } from '@/store/authStore';
import api from '@/lib/api';
import Link from 'next/link';

export default function CheckoutPage() {
  const router = useRouter();
  const { items, getTotalPrice, clearCart } = useCartStore();
  const { user, token } = useAuthStore();
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  
  // Champs d'adresse
  const [adresse, setAdresse] = useState('');
  const [ville, setVille] = useState('');
  const [codePostal, setCodePostal] = useState('');
  const [pays, setPays] = useState('France');

  useEffect(() => {
    setMounted(true);
    if (!token) {
      router.push('/login?redirect=/checkout');
    }
  }, [token, router]);

  if (!mounted) return null;
  if (!token) return <div className="p-8 text-center">Redirection vers la connexion...</div>;
  if (items.length === 0 && !success) return <div className="p-8 text-center">Votre panier est vide.</div>;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      // 1. Créer l'adresse via l'API (nécessite que /api/addresses existe ou simulé)
      // 2. Créer la commande
      const orderPayload = {
        customerId: user.id || 1, // Fallback safely
        lignesIds: items.map(item => item.id), // Adaptation selon le DTO attendu par le backend
        totalAmount: getTotalPrice(),
        address: `${adresse}, ${codePostal} ${ville}, ${pays}`
      };

      await api.post('/orders', orderPayload);
      
      // Simulation d'un paiement réussi (pourrait être remplacé par Stripe plus tard)
      setSuccess(true);
      clearCart();
      setTimeout(() => router.push('/profile'), 3000);
      
    } catch (err: any) {
      console.error("Erreur de commande:", err);
      setError(err.response?.data?.message || "Erreur lors de la validation de la commande.");
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="max-w-2xl mx-auto py-16 text-center">
        <div className="bg-green-50 p-12 rounded-2xl border border-green-100">
          <span className="text-6xl mb-4 block">✅</span>
          <h1 className="text-3xl font-bold text-green-900 mb-4">Commande confirmée !</h1>
          <p className="text-green-700">Merci pour votre achat. Vous allez être redirigé vers votre profil...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto py-8">
      <h1 className="text-3xl font-extrabold text-gray-900 mb-8">Finaliser la commande</h1>
      
      {error && (
        <div className="mb-6 p-4 bg-red-50 text-red-700 rounded-md border border-red-100">
          {error}
        </div>
      )}

      <div className="flex flex-col md:flex-row gap-8">
        <div className="flex-grow">
          <form onSubmit={handleSubmit} className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 space-y-4">
            <h2 className="text-xl font-bold text-gray-800 mb-4">Adresse de livraison</h2>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Adresse postale</label>
              <input type="text" required value={adresse} onChange={e => setAdresse(e.target.value)} className="w-full px-4 py-2 border border-gray-300 rounded-md" placeholder="123 Rue de la République" />
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Code postal</label>
                <input type="text" required value={codePostal} onChange={e => setCodePostal(e.target.value)} className="w-full px-4 py-2 border border-gray-300 rounded-md" placeholder="75001" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Ville</label>
                <input type="text" required value={ville} onChange={e => setVille(e.target.value)} className="w-full px-4 py-2 border border-gray-300 rounded-md" placeholder="Paris" />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Pays</label>
              <input type="text" required value={pays} onChange={e => setPays(e.target.value)} className="w-full px-4 py-2 border border-gray-300 rounded-md" />
            </div>

            <hr className="my-6" />
            
            <h2 className="text-xl font-bold text-gray-800 mb-4">Paiement</h2>
            <div className="p-4 bg-gray-50 border border-gray-200 rounded-md">
              <p className="text-gray-600 text-sm">Le paiement par carte bancaire (Stripe) sera initialisé après confirmation.</p>
            </div>

            <button 
              type="submit" 
              disabled={loading}
              className="w-full bg-indigo-600 text-white font-bold py-4 rounded-md hover:bg-indigo-700 transition disabled:bg-indigo-400 mt-6"
            >
              {loading ? 'Traitement en cours...' : `Payer ${getTotalPrice().toFixed(2)} €`}
            </button>
          </form>
        </div>

        <div className="w-full md:w-80 shrink-0">
          <div className="bg-gray-50 p-6 rounded-2xl border border-gray-200">
            <h2 className="font-bold text-gray-800 mb-4">Votre panier</h2>
            <ul className="space-y-3 mb-4">
              {items.map(item => (
                <li key={item.id} className="flex justify-between text-sm">
                  <span className="text-gray-600 w-2/3 truncate">{item.quantity}x {item.name}</span>
                  <span className="font-medium">{(item.price * item.quantity).toFixed(2)} €</span>
                </li>
              ))}
            </ul>
            <div className="border-t border-gray-300 pt-3 flex justify-between items-center font-bold text-lg">
              <span>Total</span>
              <span>{getTotalPrice().toFixed(2)} €</span>
            </div>
            <Link href="/cart" className="text-indigo-600 text-sm mt-4 inline-block hover:underline">
              Modifier le panier
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
