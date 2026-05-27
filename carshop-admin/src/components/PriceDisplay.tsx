import type { CSSProperties } from 'react';

export interface PriceDisplayProps {
  cents: number | null | undefined;
  style?: CSSProperties;
  strike?: boolean;
}

export function PriceDisplay({ cents, style, strike }: PriceDisplayProps) {
  if (cents === null || cents === undefined) return <span style={style}>—</span>;
  const yuan = (cents / 100).toFixed(2);
  return (
    <span
      style={{
        textDecoration: strike ? 'line-through' : undefined,
        ...style,
      }}
    >
      ¥{yuan}
    </span>
  );
}
