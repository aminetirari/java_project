"use client";

export interface BarItem {
  label: string;
  value: number;
}

interface Props {
  data: BarItem[];
  currency?: string;
  colorClass?: string;
}

export default function MiniBarChart({
  data,
  currency = "€",
  colorClass = "bg-indigo-500",
}: Props) {
  if (!data.length) {
    return <p className="text-sm text-slate-500">Aucune donnée disponible.</p>;
  }
  const max = Math.max(1, ...data.map((d) => d.value));
  return (
    <ul className="space-y-2">
      {data.map((d) => {
        const pct = Math.round((d.value / max) * 100);
        return (
          <li key={d.label} className="text-sm">
            <div className="mb-1 flex items-center justify-between">
              <span className="truncate font-medium text-slate-700">{d.label}</span>
              <span className="tabular-nums text-slate-600">
                {d.value.toFixed(2)} {currency}
              </span>
            </div>
            <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
              <div
                className={`h-full ${colorClass}`}
                style={{ width: `${Math.max(4, pct)}%` }}
              />
            </div>
          </li>
        );
      })}
    </ul>
  );
}
