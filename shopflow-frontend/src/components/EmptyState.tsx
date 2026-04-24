import Link from "next/link";

interface Props {
  title: string;
  description?: string;
  ctaHref?: string;
  ctaLabel?: string;
}

export default function EmptyState({ title, description, ctaHref, ctaLabel }: Props) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-xl border border-dashed border-slate-300 bg-white px-6 py-16 text-center">
      <div className="flex h-12 w-12 items-center justify-center rounded-full bg-indigo-50 text-indigo-600">
        <svg
          className="h-6 w-6"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
        >
          <path d="M3 7h18M6 7v13a1 1 0 001 1h10a1 1 0 001-1V7M9 7V5a3 3 0 013-3 3 3 0 013 3v2" />
        </svg>
      </div>
      <h3 className="text-lg font-semibold text-slate-900">{title}</h3>
      {description && <p className="text-sm text-slate-500">{description}</p>}
      {ctaHref && ctaLabel && (
        <Link href={ctaHref} className="btn-primary mt-2">
          {ctaLabel}
        </Link>
      )}
    </div>
  );
}
