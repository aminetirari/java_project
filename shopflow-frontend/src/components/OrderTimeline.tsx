import type { OrderStatus } from "@/lib/types";

interface Step {
  key: OrderStatus;
  label: string;
  description: string;
}

const STEPS: Step[] = [
  { key: "PENDING", label: "Commandée", description: "Commande enregistrée" },
  { key: "PAID", label: "Payée", description: "Paiement confirmé" },
  { key: "PROCESSING", label: "En préparation", description: "Le vendeur prépare votre colis" },
  { key: "SHIPPED", label: "Expédiée", description: "Votre colis est en route" },
  { key: "DELIVERED", label: "Livrée", description: "Colis remis" },
];

const STATUS_INDEX: Partial<Record<OrderStatus, number>> = {
  PENDING: 0,
  PAID: 1,
  PAYE: 1,
  PROCESSING: 2,
  SHIPPED: 3,
  DELIVERED: 4,
};

export default function OrderTimeline({ status }: { status: OrderStatus }) {
  if (status === "CANCELLED") {
    return (
      <div className="rounded-lg border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
        Cette commande a été <span className="font-semibold">annulée</span>. Les
        produits ont été remis en stock.
      </div>
    );
  }

  const currentIndex = STATUS_INDEX[status] ?? 0;

  return (
    <ol className="flex flex-col gap-0 md:flex-row md:items-start md:justify-between">
      {STEPS.map((step, idx) => {
        const reached = idx <= currentIndex;
        const active = idx === currentIndex;
        return (
          <li
            key={step.key}
            className="relative flex flex-1 items-start gap-3 md:flex-col md:items-center md:text-center"
          >
            <div
              className={`flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full border-2 text-sm font-bold ${
                reached
                  ? "border-indigo-600 bg-indigo-600 text-white"
                  : "border-slate-200 bg-white text-slate-400"
              } ${active ? "ring-4 ring-indigo-100" : ""}`}
            >
              {reached ? "✓" : idx + 1}
            </div>
            {idx < STEPS.length - 1 && (
              <div
                className={`absolute left-[17px] top-9 h-[calc(100%-1.5rem)] w-0.5 md:left-1/2 md:top-[18px] md:h-0.5 md:w-full md:translate-x-1/2 ${
                  idx < currentIndex ? "bg-indigo-600" : "bg-slate-200"
                }`}
              />
            )}
            <div className="pb-6 md:pb-0 md:pt-3 md:max-w-[140px]">
              <div
                className={`text-sm font-semibold ${
                  reached ? "text-slate-900" : "text-slate-400"
                }`}
              >
                {step.label}
              </div>
              <div className="text-xs text-slate-500">{step.description}</div>
            </div>
          </li>
        );
      })}
    </ol>
  );
}
