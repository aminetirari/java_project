import Link from 'next/link';
import api from '@/lib/api';

// On va récupérer les produits via le backend, un peu différent d'un use client
async function getSellerProducts() {
  try {
    // Cette route n'existe pas encore côté backend de façon authentifiée pour les VENDEURS, 
    // Mais on peut faire : GET /products et filtrer, ou appeler un endpoint admin.
    const res = await api.get('/products');
    return res.data?.content || res.data || [];
  } catch (error) {
    return [];
  }
}

export default async function SellerProductsPage() {
  const products = await getSellerProducts();

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">Mes Produits</h1>
        <Link href="/dashboard/products/new" className="bg-indigo-600 text-white px-4 py-2 rounded-lg font-bold hover:bg-indigo-700 transition">
          Nouveau produit
        </Link>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50 text-gray-600 text-sm uppercase tracking-wider">
              <th className="px-6 py-4 font-semibold border-b border-gray-200">Produit</th>
              <th className="px-6 py-4 font-semibold border-b border-gray-200">Prix</th>
              <th className="px-6 py-4 font-semibold border-b border-gray-200">Stock</th>
              <th className="px-6 py-4 font-semibold border-b border-gray-200">Statut</th>
              <th className="px-6 py-4 font-semibold border-b border-gray-200">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 text-sm">
            {products.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-6 py-8 text-center text-gray-500">
                  Aucun produit trouvé.
                </td>
              </tr>
            ) : (
              products.map((product: any) => (
                <tr key={product.id} className="hover:bg-gray-50 transition">
                  <td className="px-6 py-4 font-medium text-gray-900">{product.nom}</td>
                  <td className="px-6 py-4 font-medium">{product.prix.toFixed(2)} €</td>
                  <td className="px-6 py-4">{product.stock}</td>
                  <td className="px-6 py-4">
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                      En ligne
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <a href={`/dashboard/products/${product.id}/edit`} className="text-indigo-600 hover:text-indigo-900 font-medium mr-4">Modifier</a>
                    <button className="text-red-600 hover:text-red-900 font-medium">Supprimer</button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}