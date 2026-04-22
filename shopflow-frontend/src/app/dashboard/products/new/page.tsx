'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';
import api from '@/lib/api';
import Link from 'next/link';

export default function NewProductPage() {
  const router = useRouter();
  const { user } = useAuthStore();
  
  const [formData, setFormData] = useState({
    nom: '',
    description: '',
    prix: '',
    prixPromo: '',
    stock: '1'
  });
  const [files, setFiles] = useState<FileList | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setFiles(e.target.files);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      if (!user?.id) {
        throw new Error("Informations utilisateur manquantes. Veuillez vous reconnecter.");
      }

      const payload = {
        nom: formData.nom,
        description: formData.description,
        prix: parseFloat(formData.prix),
        prixPromo: formData.prixPromo ? parseFloat(formData.prixPromo) : null,
        stock: parseInt(formData.stock, 10),
        sellerId: user.id,
        categoryIds: [],
        images: [],
        variantes: []
      };

      // 1. Création du produit
      const response = await api.post('/products', payload);
      const productId = response.data.id;

      // 2. Upload des images (s'il y en a)
      if (files && files.length > 0 && productId) {
        const imageFormData = new FormData();
        Array.from(files).forEach((file) => {
          imageFormData.append('files', file);
        });

        await api.post(`/products/${productId}/images`, imageFormData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });
      }
      
      router.push('/dashboard/products');
      router.refresh();
    } catch (err: any) {
      console.error(err);
      setError(err.response?.data?.message || 'Erreur lors de la création du produit. Vérifiez les champs fournis.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Ajouter un produit</h1>
        <Link href="/dashboard/products" className="text-indigo-600 hover:text-indigo-900 font-medium">
          &larr; Retour
        </Link>
      </div>

      {error && (
        <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-6 rounded-md">
          <p className="text-red-700 font-medium">{error}</p>
        </div>
      )}

      <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 space-y-6">
        <div>
          <label htmlFor="nom" className="block text-sm font-semibold text-gray-700 mb-2">Nom du produit *</label>
          <input 
            type="text" 
            id="nom" 
            name="nom" 
            required 
            value={formData.nom}
            onChange={handleChange}
            className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition"
            placeholder="Ex: T-shirt en coton bio"
          />
        </div>

        <div>
          <label htmlFor="description" className="block text-sm font-semibold text-gray-700 mb-2">Description</label>
          <textarea 
            id="description" 
            name="description" 
            rows={4}
            value={formData.description}
            onChange={handleChange}
            className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition"
            placeholder="Décrivez votre produit en quelques mots..."
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label htmlFor="prix" className="block text-sm font-semibold text-gray-700 mb-2">Prix (€) *</label>
            <input 
              type="number" 
              id="prix" 
              name="prix" 
              step="0.01"
              min="0"
              required 
              value={formData.prix}
              onChange={handleChange}
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition"
              placeholder="0.00"
            />
          </div>
          <div>
            <label htmlFor="prixPromo" className="block text-sm font-semibold text-gray-700 mb-2">Prix Promotionnel (€)</label>
            <input 
              type="number" 
              id="prixPromo" 
              name="prixPromo" 
              step="0.01"
              min="0"
              value={formData.prixPromo}
              onChange={handleChange}
              className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition"
              placeholder="Optionnel"
            />
          </div>
        </div>

        <div>
          <label htmlFor="stock" className="block text-sm font-semibold text-gray-700 mb-2">Quantité en stock *</label>
          <input 
            type="number" 
            id="stock" 
            name="stock" 
            min="0"
            required 
            value={formData.stock}
            onChange={handleChange}
            className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition"
            placeholder="10"
          />
        </div>

        <div>
          <label htmlFor="images" className="block text-sm font-semibold text-gray-700 mb-2">Images du produit</label>
          <input 
            type="file" 
            id="images" 
            name="images" 
            multiple
            accept="image/*"
            onChange={handleFileChange}
            className="w-full px-4 py-3 rounded-lg border border-gray-200 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 outline-none transition file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100"
          />
          <p className="text-xs text-gray-500 mt-2">Vous pouvez sélectionner plusieurs images. Formats acceptés : JPG, PNG.</p>
        </div>

        <div className="pt-6 border-t border-gray-100 flex justify-end">
          <button 
            type="submit" 
            disabled={loading}
            className="bg-indigo-600 text-white font-bold px-8 py-3 rounded-lg hover:bg-indigo-700 transition shadow-sm disabled:opacity-70 disabled:cursor-not-allowed"
          >
            {loading ? 'Création en cours...' : 'Publier le produit'}
          </button>
        </div>
      </form>
    </div>
  );
}