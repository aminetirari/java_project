'use client';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';
import api from '@/lib/api';

export default function ProfilePage() {
  const { user, token } = useAuthStore();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setMounted(true);
    if (!token) {
      router.push('/login');
      return;
    }

    const fetchOrders = async () => {
      try {
        // En vrai, il faudrait un endpoint GET /api/orders/user/{id} dans Spring Boot
        const response = await api.get(`/orders`);
        setOrders(response.data?.content || response.data || []);
      } catch (err) {
        console.error("Erreur récupération commandes:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, [token, router]);

  if (!mounted || !token) return null;

  return (
    <div className="max-w-4xl mx-auto py-12">
      <h1 className="text-3xl font-extrabold text-gray-900 mb-8">Bonjour, {user?.nom || 'Utilisateur'}</h1>

      <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 mb-8">
        <h2 className="text-xl font-bold text-gray-800 mb-4">Mes Informations</h2>
        <p className="text-gray-600 mb-2"><strong>Email : </strong> {user?.email}</p>
        <p className="text-gray-600"><strong>Nom d'utilisateur : </strong> {user?.username}</p>
      </div>

      <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100">
        <h2 className="text-xl font-bold text-gray-800 mb-6">Mes Commandes</h2>
        
        {loading ? (
          <p className="text-gray-500">Chargement de vos commandes...</p>
        ) : orders.length === 0 ? (
          <div className="text-center py-8">
            <span className="text-4xl mb-4 block">📦</span>
            <p className="text-gray-500">Vous n'avez pas encore passé de commande.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order: any, index: number) => (
              <div key={order.id || index} className="border border-gray-200 rounded-lg p-4 flex justify-between items-center">
                <div>
                  <h3 className="font-bold text-indigo-600">Commande #{order.id || index + 1}</h3>
                  <p className="text-sm text-gray-500 line-clamp-1">{order.address || 'Adresse non spécifiée'}</p>
                </div>
                <div className="text-right">
                  <p className="font-bold text-gray-900">{order.totalAmount ? order.totalAmount.toFixed(2) : '0.00'} €</p>
                  <span className="text-xs bg-indigo-100 text-indigo-800 px-2 py-1 rounded-md font-medium mt-1 inline-block">
                    {order.status || 'En cours'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
