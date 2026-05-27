// 从 design/安卓商城/tokens.json 翻译过来 · v1.0.0 · 海泡青电车感
// 与车机端互不共享代码,各自从 tokens.json 独立翻译。

export const colors = {
  // primitive
  steel0: '#FFFFFF',
  steel50: '#F5F7F9',
  steel100: '#E8EDF1',
  steel200: '#D4DCE4',
  steel300: '#B8C5CF',
  steel400: '#8FA4B1',
  steel500: '#5D7B8A',
  steel700: '#2A3D49',
  steel800: '#1A2027',

  seafoam50: '#E6FBF6',
  seafoam500: '#00C2A8',
  seafoam600: '#00A892',
  seafoam700: '#008270',

  signal50: '#FEF2F3',
  signal500: '#E63946',
  signal700: '#A91823',

  // semantic
  primary: '#1A2027',
  secondary: '#5D7B8A',
  tertiary: '#00A892',
  error: '#E63946',
  background: '#F5F7F9',
  surface: '#FFFFFF',
  outline: '#D4DCE4',

  // 业务专用
  textPrice: '#E63946',
  textPriceEnergy: '#00A892',
  textPriceStrike: '#B8C5CF',
  brandSeafoam: '#00C2A8',
} as const;

export const radius = {
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  full: 9999,
} as const;

export const fontFamilySans =
  '"Source Han Sans SC","Noto Sans SC",system-ui,-apple-system,"PingFang SC","Microsoft YaHei",sans-serif';

export const fontFamilyMono =
  '"JetBrains Mono","Roboto Mono",ui-monospace,SFMono-Regular,Menlo,monospace';
