import React, { useEffect } from 'react';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import * as SplashScreen from 'expo-splash-screen';
import {
  useFonts,
  Fraunces_400Regular,
  Fraunces_400Regular_Italic,
  Fraunces_600SemiBold,
  Fraunces_700Bold,
} from '@expo-google-fonts/fraunces';
import {
  WorkSans_400Regular,
  WorkSans_500Medium,
  WorkSans_600SemiBold,
} from '@expo-google-fonts/work-sans';
import { ThemeProvider, useTheme } from '../src/theme/ThemeContext';

SplashScreen.preventAutoHideAsync().catch(() => {});

function RootLayoutContent() {
  const { colors, isDark } = useTheme();

  return (
    <>
      <StatusBar style={isDark ? 'light' : 'dark'} />
      <Stack
        screenOptions={{
          headerStyle: {
            backgroundColor: colors.background,
          },
          headerTintColor: colors.textPrimary,
          headerTitleStyle: {
            fontFamily: 'Fraunces_600SemiBold',
            fontSize: 20,
          },
          contentStyle: {
            backgroundColor: colors.background,
          },
          headerShadowVisible: false,
        }}
      >
        <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        <Stack.Screen name="first-open" options={{ headerShown: false, presentation: 'fullScreenModal' }} />
        <Stack.Screen name="add" options={{ title: 'New Affirmation', headerBackTitle: 'Cancel' }} />
        <Stack.Screen name="detail" options={{ title: 'Affirmation', headerBackTitle: 'Back' }} />
        <Stack.Screen name="image-generator" options={{ title: 'Create Image', headerBackTitle: 'Back' }} />
        <Stack.Screen name="notifications" options={{ title: 'Notifications', headerBackTitle: 'Settings' }} />
        <Stack.Screen name="widget-config" options={{ title: 'Widget Setup', headerBackTitle: 'Settings' }} />
      </Stack>
    </>
  );
}

export default function RootLayout() {
  const [fontsLoaded] = useFonts({
    Fraunces_400Regular,
    Fraunces_400Regular_Italic,
    Fraunces_600SemiBold,
    Fraunces_700Bold,
    WorkSans_400Regular,
    WorkSans_500Medium,
    WorkSans_600SemiBold,
  });

  useEffect(() => {
    if (fontsLoaded) {
      SplashScreen.hideAsync().catch(() => {});
    }
  }, [fontsLoaded]);

  if (!fontsLoaded) {
    return null;
  }

  return (
    <ThemeProvider>
      <RootLayoutContent />
    </ThemeProvider>
  );
}
