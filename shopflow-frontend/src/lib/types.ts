export type Role = "ADMIN" | "SELLER" | "CUSTOMER";

export interface User {
  id: number;
  email: string;
  prenom: string;
  nom: string;
  role: Role;
  actif?: boolean;
  dateCreation?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  type?: string;
  id: number;
  email: string;
  prenom?: string;
  nom?: string;
  roles: string[];
}

export interface ProductVariant {
  id: number;
  attribut: string;
  valeur: string;
  stockSupplementaire?: number;
  prixDelta?: number;
}

export interface Product {
  id: number;
  sellerId: number;
  sellerNom?: string;
  nom: string;
  description?: string;
  prix: number;
  prixPromo?: number;
  stock: number;
  actif?: boolean;
  dateCreation?: string;
  categoryIds?: number[];
  images?: string[];
  variantes?: ProductVariant[];
  noteMoyenne?: number;
  nbAvis?: number;
}

export interface Category {
  id: number;
  nom: string;
  description?: string;
  parentId?: number;
  sousCategories?: Category[];
}

export interface CartItem {
  id: number;
  productId: number;
  variantId?: number;
  productNom: string;
  quantite: number;
  prixUnitaire: number;
  sousTotal: number;
}

export interface Cart {
  id: number;
  customerId: number;
  lignes: CartItem[];
  sousTotal: number;
  codePromo?: string;
  remise: number;
  totalCart: number;
}

export interface Address {
  id: number;
  userId?: number;
  rue: string;
  ville: string;
  codePostal: string;
  pays: string;
  principal?: boolean;
}

export type OrderStatus =
  | "PENDING"
  | "PAID"
  | "PAYE"
  | "PROCESSING"
  | "SHIPPED"
  | "DELIVERED"
  | "CANCELLED";

export interface OrderItem {
  id: number;
  productId: number;
  variantId?: number;
  quantite: number;
  prixUnitaire: number;
  sousTotal: number;
}

export interface Order {
  id: number;
  numeroCommande: string;
  customerId: number;
  dateCommande: string;
  paymentIntentId?: string;
  status: OrderStatus;
  sousTotal: number;
  codePromo?: string;
  montantRemise: number;
  fraisLivraison: number;
  total: number;
  lignes: OrderItem[];
}

export interface Review {
  id: number;
  customerId: number;
  customerName: string;
  productId: number;
  note: number;
  commentaire: string;
  dateCreation?: string;
  approuve: boolean;
}

export type CouponType = "PERCENT" | "FIXED";

export interface Coupon {
  id: number;
  code: string;
  type: CouponType;
  valeur: number;
  dateExpiration?: string;
  usagesMax?: number;
  usagesActuels?: number;
  actif?: boolean;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ApiError {
  message?: string;
  status?: number;
  errors?: Record<string, string>;
}
