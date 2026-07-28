import React, { createContext, useContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { ThemeType, ThemeColors, lightTheme, darkTheme } from './colors';

const STORAGE_KEY = '@makarios/theme';

interface ThemeContextType {
  theme: ThemeType;
  colors: ThemeColors;
  toggleTheme: () => void;
  isDark: boolean;
}

const ThemeContext = createContext<ThemeContextType>({
  theme: 'light',
  colors: lightTheme,
  toggleTheme: () => {},
  isDark: false,
});

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [theme, setTheme] = useState<ThemeType>('light');

  useEffect(() => {
    AsyncStorage.getItem(STORAGE_KEY).then((savedTheme) => {
      if (savedTheme === 'light' || savedTheme === 'dark') {
        setTheme(savedTheme);
      }
    }).catch(() => {});
  }, []);

  const toggleTheme = () => {
    const nextTheme: ThemeType = theme === 'light' ? 'dark' : 'light';
    setTheme(nextTheme);
    AsyncStorage.setItem(STORAGE_KEY, nextTheme).catch(() => {});
  };

  const colors = theme === 'light' ? lightTheme : darkTheme;
  const isDark = theme === 'dark';

  return (
    <ThemeContext.Provider value={{ theme, colors, toggleTheme, isDark }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => useContext(ThemeContext);
