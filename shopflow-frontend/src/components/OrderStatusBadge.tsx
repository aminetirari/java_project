import type { OrderStatus } from "@/lib/types";

const LABELS: Record<OrderStatus, { text: string; cls: string }> = {
  PENDING: { text: "En attente", cls: "bg-amber-50 text-amber-700 border-amber-200" },
  PAID: { text: "Payée", cls: "bg-blue-50 text-blue-700 border-blue-200" },
  PAYE: { text: "Payée", cls: "bg-blue-50 text-blue-700 border-blue-200" },
  PROCESSING: {
    text: "En préparation",
    cls: "bg-indigo-50 text-indigo-700 border-indigo-200",
  },
  SHIPPED: {
    text: "Expédiée",
    cls: "bg-purple-50 text-purple-700 border-purple-200",
  },
  DELIVERED: {
    text: "Livrée",
    cls: "bg-emerald-50 text-emerald-700 border-emerald-200",
  },
  CANCELLED: { text: "Annulée", cls: "bg-rose-50 text-rose-700 border-rose-200" },
};

export default function OrderStatusBadge({ status }: { status: OrderStatus }) {
  const info = LABELS[status] ?? { text: status, cls: "bg-slate-50 text-slate-700" };
  return (
    <span className={`badge border ${info.cls}`}>{info.text}</span>
  );
}
