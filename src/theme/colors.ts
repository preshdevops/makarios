export const PALETTE = {
  parchment: '#F6F1E7',
  ink: '#1C1A17',
  inkElevated: '#26231F',
  goldLeaf: '#C08A3E',
  goldDark: '#9A6E2E',
  dustyRose: '#B5654B',
  mutedSage: '#6B7F5C',
  mutedRust: '#A8482F',
  inkBorder: 'rgba(28, 26, 23, 0.15)',
  parchmentBorder: 'rgba(246, 241, 231, 0.15)',
};

export type ThemeType = 'light' | 'dark';

export interface ThemeColors {
  background: string;
  surface: string;
  surfaceElevated: string;
  textPrimary: string;
  textSecondary: string;
  textMuted: string;
  accent: string;
  accentText: string;
  secondary: string;
  success: string;
  error: string;
  border: string;
  cardBg: string;
  cardBorder: string;
  inputBg: string;
  tabBarBg: string;
  tabBarBorder: string;
  chipBg: string;
  chipSelectedBg: string;
  chipSelectedText: string;
}

export const lightTheme: ThemeColors = {
  background: PALETTE.parchment,
  surface: PALETTE.parchment,
  surfaceElevated: '#EFEAE0',
  textPrimary: PALETTE.ink,
  textSecondary: 'rgba(28, 26, 23, 0.75)',
  textMuted: 'rgba(28, 26, 23, 0.5)',
  accent: PALETTE.goldLeaf,
  accentText: PALETTE.goldDark,
  secondary: PALETTE.dustyRose,
  success: PALETTE.mutedSage,
  error: PALETTE.mutedRust,
  border: PALETTE.inkBorder,
  cardBg: PALETTE.parchment,
  cardBorder: PALETTE.inkBorder,
  inputBg: '#EDE7DC',
  tabBarBg: PALETTE.parchment,
  tabBarBorder: PALETTE.inkBorder,
  chipBg: 'rgba(28, 26, 23, 0.06)',
  chipSelectedBg: PALETTE.goldLeaf,
  chipSelectedText: PALETTE.ink,
};

export const darkTheme: ThemeColors = {
  background: PALETTE.ink,
  surface: PALETTE.ink,
  surfaceElevated: PALETTE.inkElevated,
  textPrimary: PALETTE.parchment,
  textSecondary: 'rgba(246, 241, 231, 0.75)',
  textMuted: 'rgba(246, 241, 231, 0.5)',
  accent: PALETTE.goldLeaf,
  accentText: PALETTE.goldLeaf,
  secondary: PALETTE.dustyRose,
  success: PALETTE.mutedSage,
  error: PALETTE.mutedRust,
  border: PALETTE.parchmentBorder,
  cardBg: PALETTE.inkElevated,
  cardBorder: PALETTE.parchmentBorder,
  inputBg: '#2E2A25',
  tabBarBg: PALETTE.ink,
  tabBarBorder: PALETTE.parchmentBorder,
  chipBg: 'rgba(246, 241, 231, 0.08)',
  chipSelectedBg: PALETTE.goldLeaf,
  chipSelectedText: PALETTE.ink,
};
