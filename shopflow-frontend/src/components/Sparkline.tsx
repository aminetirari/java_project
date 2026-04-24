"use client";

import { useMemo } from "react";

export interface SparklinePoint {
  date: string;
  total: number | string;
}

interface Props {
  data: SparklinePoint[];
  color?: string;
  height?: number;
  fill?: boolean;
  currency?: string;
}

export default function Sparkline({
  data,
  color = "#4f46e5",
  height = 80,
  fill = true,
  currency = "€",
}: Props) {
  const { path, area, max, last, width } = useMemo(() => {
    const values = data.map((p) => Number(p.total) || 0);
    const maxV = Math.max(1, ...values);
    const stepW = 100 / Math.max(1, values.length - 1);
    const points = values.map((v, i) => {
      const x = i * stepW;
      const y = 100 - (v / maxV) * 100;
      return [x, y] as const;
    });
    const pathD =
      points
        .map((p, i) => `${i === 0 ? "M" : "L"}${p[0].toFixed(2)},${p[1].toFixed(2)}`)
        .join(" ");
    const areaD =
      points.length > 0
        ? `${pathD} L${points[points.length - 1][0].toFixed(2)},100 L${points[0][0].toFixed(
            2
          )},100 Z`
        : "";
    return {
      path: pathD,
      area: areaD,
      max: maxV,
      last: values[values.length - 1] ?? 0,
      width: 100,
    };
  }, [data]);

  const total = useMemo(
    () => data.reduce((acc, p) => acc + (Number(p.total) || 0), 0),
    [data]
  );

  return (
    <div>
      <div className="mb-2 flex items-baseline justify-between text-xs text-slate-500">
        <span>Total période : <span className="font-semibold text-slate-900">{total.toFixed(2)} {currency}</span></span>
        <span>Pic : {max.toFixed(2)} {currency}</span>
      </div>
      <svg
        viewBox={`0 0 ${width} 100`}
        preserveAspectRatio="none"
        width="100%"
        height={height}
        className="block"
      >
        <defs>
          <linearGradient id="sparkFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stopColor={color} stopOpacity="0.35" />
            <stop offset="100%" stopColor={color} stopOpacity="0" />
          </linearGradient>
        </defs>
        {fill && area && <path d={area} fill="url(#sparkFill)" />}
        {path && <path d={path} fill="none" stroke={color} strokeWidth={1.5} />}
      </svg>
      <div className="mt-1 flex justify-between text-[10px] text-slate-400">
        <span>{data[0]?.date ?? ""}</span>
        <span>dernière valeur : {last.toFixed(2)} {currency}</span>
        <span>{data[data.length - 1]?.date ?? ""}</span>
      </div>
    </div>
  );
}
