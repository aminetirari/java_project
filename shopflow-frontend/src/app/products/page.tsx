import Link from 'next/link';
import api from '@/lib/api';

async function getProducts() {
  try {
    const response = await api.get('/products');
    return response.data?.content || response.data || [];
  } catch (error) {
    console.error("Error fetching products", error);
    return [];
  }
}

async function getCategories() {
  try {
    const response = await api.get('/categories');
    return response.data?.content || response.data || [];
  } catch (error) {
    console.error("Error fetching categories", error);
    return [];
  }
}

export default async function ProductsPage({ searchParams }: { searchParams: { category?: string } }) {
  const [products, categories] = await Promise.all([getProducts(), getCategories()]);
  
  const selectedCategory = searchParams.category;
  
  const displayedProducts = selectedCategory 
    ? products.filter((p: any) => p.category?.id.toString() === selectedCategory)
    : products;

  return (
    <div className="flex flex-col md:flex-row gap-8 py-8">
      {/* Sidebar: Catégories */}
      <aside className="w-full md:w-64 shrink-0">
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <h2 className="font-bold text-lg text-gray-800 mb-4">Catégories</h2>
          <ul className="space-y-2">
            <li>
              <Link 
                href="/products" 
                className={`block px-3 py-2 rounded-md transition ${!selectedCategory ? 'bg-indigo-50 text-indigo-700 font-bold' : 'text-gray-600 hover:bg-gray-50'}`}
              >
                Toutes les catégories
              </Link>
            </li>
            {categories.map((cat: any) => (
              <li key={cat.id}>
                <Link 
                  href={`/products?category=${cat.id}`}
                  className={`block px-3 py-2 rounded-md transition ${selectedCategory === cat.id.toString() ? 'bg-indigo-50 text-indigo-700 font-bold' : 'text-gray-600 hover:bg-gray-50'}`}
                >
                  {cat.nom || cat.name}
                </Link>
              </li>
            ))}
          </ul>
        </div>
      </aside>

      {/* Main Content: Liste des produits */}
      <div className="flex-grow">
        <div className="mb-6 flex justify-between items-center bg-white p-4 rounded-xl shadow-sm border border-gray-100">
          <h1 className="text-2xl font-bold text-gray-800">
            {selectedCategory 
              ? categories.find((c: any) => c.id.toString() === selectedCategory)?.nom || 'Produits'
              : 'Tous nos produits'
            }
          </h1>
          <span className="text-gray-500 font-medium">{displayedProducts.length} résultat(s)</span>
        </div>

        {displayedProducts.length === 0 ? (
          <div className="bg-white rounded-xl shadow-sm p-16 text-center border border-gray-100">
            <span className="text-6xl mb-4 block">🔍</span>
            <h3 className="text-xl font-bold text-gray-700 mb-2">Aucun produit trouvé</h3>
            <p className="text-gray-500">Essayez de sélectionner une autre catégorie.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {displayedProducts.map((product: any) => {
              const imageUrl = product.images && product.images.length > 0 
                ? `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'}/products/images/download/${product.images[0]}` 
                : null;

              return (
                <Link href={`/products/${product.id}`} key={product.id} className="group bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-xl transition flex flex-col items-center p-4">
                  <div className="aspect-square bg-gray-50 w-full rounded-lg mb-4 flex items-center justify-center overflow-hidden relative">
                    {imageUrl ? (
                      <img src={imageUrl} alt={product.nom} className="object-cover w-full h-full group-hover:scale-105 transition-transform duration-300" />
                    ) : (
                      <span className="text-5xl text-gray-300 group-hover:scale-110 transition-transform">📦</span>
                    )}
                  </div>
                  <div className="p-2 flex-grow w-full text-left">
                    <div className="text-xs text-indigo-500 font-bold uppercase tracking-wider mb-1">
                      {product.category?.nom || product.category?.name || 'Général'}
                    </div>
                    <h3 className="font-bold text-gray-800 mb-1 group-hover:text-indigo-600 transition truncate">{product.nom}</h3>
                    <p className="text-2xl font-extrabold text-gray-900 mt-2">{product.prix?.toFixed(2) || '0.00'} €</p>
                  </div>
                </Link>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
