interface Props {
  value: number;
  count?: number;
  size?: "sm" | "md";
  interactive?: boolean;
  onChange?: (v: number) => void;
}

export default function RatingStars({
  value,
  count,
  size = "md",
  interactive = false,
  onChange,
}: Props) {
  const stars = [1, 2, 3, 4, 5];
  const dim = size === "sm" ? "h-4 w-4" : "h-5 w-5";
  return (
    <div className="flex items-center gap-1">
      <div className="flex">
        {stars.map((i) => (
          <button
            key={i}
            type="button"
            disabled={!interactive}
            onClick={() => interactive && onChange?.(i)}
            className={`${dim} ${interactive ? "cursor-pointer" : "cursor-default"} ${
              i <= Math.round(value) ? "text-amber-400" : "text-slate-300"
            }`}
            aria-label={`${i} étoile${i > 1 ? "s" : ""}`}
          >
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 17.3 6.18 20.6l1.64-6.56L2 9.24l6.73-.58L12 2.5l3.27 6.16 6.73.58-5.82 4.8 1.64 6.56z" />
            </svg>
          </button>
        ))}
      </div>
      {count !== undefined && (
        <span className="text-xs text-slate-500">({count})</span>
      )}
    </div>
  );
}
