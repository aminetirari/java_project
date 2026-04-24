import type { ReactNode } from "react";

interface Props {
  label: string;
  value: ReactNode;
  hint?: ReactNode;
  accent?: string;
  trend?: number | null;
}

export default function KpiCard({
  label,
  value,
  hint,
  accent = "border-l-indigo-500",
  trend,
}: Props) {
  const trendColor =
    trend === undefined || trend === null
      ? null
      : trend >= 0
      ? "text-emerald-600 bg-emerald-50"
      : "text-rose-600 bg-rose-50";
  return (
    <div className={`card border-l-4 p-5 ${accent}`}>
      <p className="text-sm text-slate-500">{label}</p>
      <p className="mt-1 text-2xl font-bold text-slate-900">{value}</p>
      <div className="mt-1 flex items-center gap-2 text-xs text-slate-500">
        {hint && <span className="truncate">{hint}</span>}
        {trendColor && trend !== undefined && trend !== null && (
          <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${trendColor}`}>
            {trend >= 0 ? "▲" : "▼"} {Math.abs(trend).toFixed(1)} %
          </span>
        )}
      </div>
    </div>
  );
}
