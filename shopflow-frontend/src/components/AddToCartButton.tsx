'use client';
import { useState } from 'react';
import { useCartStore } from '@/store/cartStore';

export default function AddToCartButton({ product }: { product: any }) {
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(false);
  const addItem = useCartStore((state) => state.addItem);

  const handleAddToCart = () => {
    setLoading(true);
    addItem({
      id: product.id,
      name: product.name,
      price: product.price,
      quantity,
    });
    
    setTimeout(() => {
      setLoading(false);
      // alert('Produit ajouté au panier !'); // Feedback optionnel, on peut utiliser un toast plus tard
    }, 300);
  };

  return (
    <div className="flex items-center gap-4 mt-6">
      <div className="flex items-center border border-gray-300 rounded-md">
        <button 
          className="px-4 py-2 text-gray-600 hover:bg-gray-100 font-bold"
          onClick={() => setQuantity(Math.max(1, quantity - 1))}
        >-</button>
        <span className="px-4 font-medium w-12 text-center">{quantity}</span>
        <button 
          className="px-4 py-2 text-gray-600 hover:bg-gray-100 font-bold"
          onClick={() => setQuantity(quantity + 1)}
        >+</button>
      </div>
      <button 
        onClick={handleAddToCart}
        disabled={loading}
        className="flex-grow bg-indigo-600 text-white px-6 py-3 rounded-md font-bold hover:bg-indigo-700 transition disabled:bg-indigo-400"
      >
        {loading ? 'Ajout en cours...' : 'Ajouter au panier'}
      </button>
    </div>
  );
}
