import Link from 'next/link';
import api from '@/lib/api';

async function getProducts() {
  try {
    const response = await api.get('/products');
    return response.data; // Assuming Spring Boot returns array in body, or response.data.content if pageable
  } catch (error) {
    console.error("Error fetching products", error);
    return [];
  }
}

export default async function Home() {
  const productsResult = await getProducts();
  
  // Spring Boot paginated result typical structure
  const products = productsResult?.content || productsResult || [];

  return (
    <div>
      <section className="bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-2xl py-20 px-4 text-center mb-12">
        <h1 className="text-4xl md:text-6xl font-extrabold mb-6">Bienvenue sur ShopFlow</h1>
        <p className="text-xl md:text-2xl mb-8 max-w-2xl mx-auto">Découvrez les meilleurs produits aux meilleurs prix. Votre nouvelle plateforme e-commerce.</p>
        <Link href="/products" className="bg-white text-indigo-600 px-8 py-4 rounded-full font-bold text-lg hover:bg-gray-100 transition shadow-lg inline-block">
          Voir la Collection
        </Link>
      </section>

      <section className="mb-12">
        <div className="flex justify-between items-center mb-8">
          <h2 className="text-3xl font-bold text-gray-800">Produits Récents</h2>
          <Link href="/products" className="text-indigo-600 font-medium hover:underline">
            Voir tout &rarr;
          </Link>
        </div>
        
        {products.length === 0 ? (
          <div className="bg-white rounded-xl shadow-sm p-12 text-center text-gray-500">
            <p>Aucun produit disponible pour le moment.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {products.map((product: any) => {
              const imageUrl = product.images && product.images.length > 0 
                ? `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'}/products/images/download/${product.images[0]}` 
                : null;
                
              return (
                <Link href={`/products/${product.id}`} key={product.id} className="group bg-white rounded-xl shadow-sm overflow-hidden hover:shadow-xl transition flex flex-col items-center p-4">
                  <div className="aspect-square bg-gray-100 w-full rounded-lg mb-4 flex items-center justify-center overflow-hidden">
                    {imageUrl ? (
                      <img src={imageUrl} alt={product.nom} className="object-cover w-full h-full group-hover:scale-105 transition-transform duration-300" />
                    ) : (
                      <span className="text-4xl text-gray-300">📦</span>
                    )}
                  </div>
                  <div className="p-4 flex-grow w-full text-center">
                    <h3 className="font-bold text-gray-800 mb-2 group-hover:text-indigo-600 transition truncate">{product.nom}</h3>
                    <p className="text-gray-500 text-sm mb-4 line-clamp-2">{product.description}</p>
                    <p className="text-xl font-extrabold text-indigo-600">{product.prix.toFixed(2)} €</p>
                  </div>
                </Link>
              );
            })}
          </div>
        )}
      </section>
    </div>
  )
}
