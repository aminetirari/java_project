import api from '@/lib/api';
import { notFound } from 'next/navigation';
import AddToCartButton from '@/components/AddToCartButton';
import Link from 'next/link';

async function getProduct(id: string) {
  try {
    const response = await api.get(`/products/${id}`);
    return response.data;
  } catch (error: any) {
    if (error.response?.status === 404) return null;
    console.error("Error fetching product", error.message);
    return null;
  }
}

export default async function ProductPage({ params }: { params: { id: string } }) {
  const product = await getProduct(params.id);

  if (!product) {
    notFound();
  }
  
  const imageUrl = product.images && product.images.length > 0 
    ? `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'}/products/images/download/${product.images[0]}` 
    : null;

  return (
    <div className="max-w-4xl mx-auto py-8">
      <Link href="/products" className="text-indigo-600 hover:underline mb-8 inline-block font-medium">
        &larr; Retour aux produits
      </Link>
      
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden flex flex-col md:flex-row">
        <div className="md:w-1/2 bg-gray-50 flex items-center justify-center p-0 aspect-square md:aspect-auto max-h-[500px]">
          {imageUrl ? (
             <img src={imageUrl} alt={product.nom} className="object-cover w-full h-full" />
          ) : (
             <span className="text-8xl">📦</span>
          )}
        </div>
        <div className="p-8 md:w-1/2 flex flex-col justify-center">
          <div className="text-sm text-indigo-600 font-bold mb-2 uppercase tracking-wide">
            {product.category?.nom || product.category?.name || 'Catégorie non spécifiée'}
          </div>
          <h1 className="text-3xl font-extrabold text-gray-900 mb-4">{product.nom}</h1>
          <p className="text-gray-600 mb-6 leading-relaxed whitespace-pre-wrap">
            {product.description || "Aucune description fournie."}
          </p>
          <div className="text-4xl font-black text-indigo-600 mb-8">
            {typeof product.prix === 'number' ? product.prix.toFixed(2) : product.prix} €
          </div>
          
          <div className="border-t border-gray-100 pt-6">
            <span className="text-sm font-medium text-gray-500 mb-2 block">
              Stock disponible : {product.stock}
            </span>
            {product.stock > 0 ? (
              <AddToCartButton product={{...product, name: product.nom, price: product.prix, quantity: 1}} />
            ) : (
              <div className="bg-red-50 text-red-600 px-4 py-3 rounded-md font-bold text-center mt-4">
                Rupture de stock
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
