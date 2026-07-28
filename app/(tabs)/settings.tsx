import React from 'react';
import { StyleSheet, Text, View, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';
import { useTheme } from '../../src/theme/ThemeContext';
import { TYPOGRAPHY } from '../../src/theme/typography';
import { SPACING } from '../../src/theme/spacing';
import { SettingsRow } from '../../src/components/SettingsRow';

export default function SettingsScreen() {
  const { colors, theme, toggleTheme } = useTheme();
  const router = useRouter();

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>
          APPEARANCE & THEME
        </Text>

        <View style={[styles.cardGroup, { borderColor: colors.border, backgroundColor: colors.surface }]}>
          <SettingsRow
            label="Parchment & Ink Theme"
            subtitle={theme === 'light' ? 'Parchment Surface (Light)' : 'Ink Base (Dark)'}
            type="switch"
            value={theme === 'dark'}
            onValueChange={toggleTheme}
          />
        </View>

        <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>
          DAILY REMINDERS & WIDGET
        </Text>

        <View style={[styles.cardGroup, { borderColor: colors.border, backgroundColor: colors.surface }]}>
          <SettingsRow
            label="Scheduled Notifications"
            subtitle="Configure delivery intervals and quiet hours"
            type="link"
            onPress={() => router.push('/notifications')}
          />
          <SettingsRow
            label="Home-Screen Widget"
            subtitle="Configure size and refreshment sources"
            type="link"
            onPress={() => router.push('/widget-config')}
          />
        </View>

        <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>
          ABOUT MAKARIOS
        </Text>

        <View style={[styles.aboutCard, { borderColor: colors.border, backgroundColor: colors.cardBg }]}>
          <Text style={[styles.aboutTitle, { color: colors.textPrimary }]}>
            Makarios (μακάριος)
          </Text>
          <Text style={[styles.aboutBody, { color: colors.textSecondary }]}>
            Greek for "blessed" or "flourishing". Rooted in the Beatitudes of Jesus (Matthew 5). Designed to feel like a personal illuminated manuscript kept close to the heart.
          </Text>
          <Text style={[styles.versionText, { color: colors.accentText }]}>
            Version 1.0.0 — Local-Only Device Storage
          </Text>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    padding: SPACING.lg,
  },
  sectionTitle: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.xs,
    letterSpacing: 0.8,
    marginBottom: SPACING.xs,
    marginTop: SPACING.md,
  },
  cardGroup: {
    borderRadius: SPACING.cardRadius,
    borderWidth: 1,
    overflow: 'hidden',
  },
  aboutCard: {
    borderRadius: SPACING.cardRadius,
    borderWidth: 1,
    padding: SPACING.lg,
    marginTop: SPACING.xs,
    marginBottom: SPACING.xl,
  },
  aboutTitle: {
    fontFamily: TYPOGRAPHY.fontDisplayBold,
    fontSize: TYPOGRAPHY.sizes.xl,
    marginBottom: SPACING.xs,
  },
  aboutBody: {
    fontFamily: TYPOGRAPHY.fontBody,
    fontSize: TYPOGRAPHY.sizes.sm,
    lineHeight: 22,
  },
  versionText: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.xs,
    marginTop: SPACING.md,
    letterSpacing: 0.5,
  },
});
