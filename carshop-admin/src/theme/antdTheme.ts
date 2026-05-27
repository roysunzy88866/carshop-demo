import type { ThemeConfig } from 'antd';
import { colors, radius, fontFamilySans } from './tokens';

// AntD 5 ConfigProvider · 只覆盖主色和圆角等关键 token,其他保持 AntD 默认。
// 后台是内部 Demo,不做精细化定制(SPEC 决策)。
export const carshopAntdTheme: ThemeConfig = {
  token: {
    colorPrimary: colors.primary,
    colorInfo: colors.tertiary,
    colorSuccess: colors.tertiary,
    colorError: colors.error,
    colorWarning: colors.error,
    colorBgBase: colors.background,
    colorTextBase: colors.primary,
    colorBorder: colors.outline,
    borderRadius: radius.md,
    fontFamily: fontFamilySans,
  },
  components: {
    Button: {
      controlHeight: 40,
      controlHeightLG: 48,
    },
    Card: {
      borderRadiusLG: radius.md,
    },
  },
};
