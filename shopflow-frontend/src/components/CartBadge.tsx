'use client';
import { useCartStore } from '@/store/cartStore';
import { useEffect, useState } from 'react';

export default function CartBadge() {
  const [mounted, setMounted] = useState(false);
  const items = useCartStore((state) => state.items);
  
  useEffect(() => {
    setMounted(true);
  }, []);

  const totalItems = items.reduce((total, item) => total + item.quantity, 0);

  if (!mounted) return null;

  return (
    <span className="ml-1 bg-red-500 text-white text-xs font-bold px-2 py-0.5 rounded-full">
      {totalItems}
    </span>
  );
}
