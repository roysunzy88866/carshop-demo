import { InputNumber } from 'antd';

// 显示元(¥XX.XX),内部 onChange 永远吐分(int)。
// SPEC 共享约定 1:金额单位用「分」,只在前端显示时除 100。
export interface PriceInputProps {
  value?: number | null; // 分(int),Form 注入时是分
  onChange?: (cents: number | null) => void;
  placeholder?: string;
  disabled?: boolean;
}

export function PriceInput({ value, onChange, placeholder, disabled }: PriceInputProps) {
  const yuan = value === null || value === undefined ? null : value / 100;
  return (
    <InputNumber
      style={{ width: '100%' }}
      value={yuan}
      min={0}
      precision={2}
      step={0.01}
      prefix="¥"
      placeholder={placeholder}
      disabled={disabled}
      onChange={(v) => {
        if (v === null || v === undefined) {
          onChange?.(null);
        } else {
          // 用 Math.round 避开浮点尾巴(99.99 * 100 = 9998.999999...)
          onChange?.(Math.round(Number(v) * 100));
        }
      }}
    />
  );
}
