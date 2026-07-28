import React, { useEffect } from 'react';
import { StyleSheet, Text, View, SafeAreaView } from 'react-native';
import { useRouter } from 'expo-router';
import { useTheme } from '../src/theme/ThemeContext';
import { TYPOGRAPHY } from '../src/theme/typography';
import { SPACING } from '../src/theme/spacing';
import { ManuscriptCard } from '../src/components/ManuscriptCard';
import { Button } from '../src/components/Button';
import { STARTER_AFFIRMATION } from '../src/data/curatedAffirmations';
import { Storage } from '../src/data/storage';

export default function FirstOpenScreen() {
  const { colors } = useTheme();
  const router = useRouter();

  const handleAddOwn = async () => {
    await Storage.markFirstOpenDone();
    router.replace('/(tabs)');
    router.push('/add');
  };

  const handleBrowseLibrary = async () => {
    await Storage.markFirstOpenDone();
    router.replace('/(tabs)/browse');
  };

  return (
    <SafeAreaView style={[styles.safeArea, { backgroundColor: colors.background }]}>
      <View style={styles.container}>
        <View style={styles.header}>
          <Text style={[styles.title, { color: colors.textPrimary }]}>Makarios</Text>
          <Text style={[styles.subtitle, { color: colors.textSecondary }]}>
            Beatitude Affirmations for Daily Life
          </Text>
        </View>

        <View style={styles.cardWrapper}>
          <ManuscriptCard
            affirmation={STARTER_AFFIRMATION}
            size="large"
            align="center"
            showTags={true}
          />
        </View>

        <View style={styles.actions}>
          <Button
            title="Add your own"
            onPress={handleAddOwn}
            variant="primary"
            style={styles.primaryButton}
          />

          <Button
            title="Browse the library"
            onPress={handleBrowseLibrary}
            variant="text"
            style={styles.secondaryButton}
          />
        </View>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
  },
  container: {
    flex: 1,
    paddingHorizontal: SPACING.lg,
    paddingVertical: SPACING.xl,
    justifyContent: 'space-between',
  },
  header: {
    alignItems: 'center',
    marginTop: SPACING.lg,
  },
  title: {
    fontFamily: TYPOGRAPHY.fontDisplayBold,
    fontSize: 36,
    letterSpacing: 0.5,
  },
  subtitle: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.sm,
    marginTop: SPACING.xs,
    letterSpacing: 0.2,
  },
  cardWrapper: {
    marginVertical: SPACING.xl,
    justifyContent: 'center',
  },
  actions: {
    width: '100%',
    marginBottom: SPACING.lg,
    gap: SPACING.md,
  },
  primaryButton: {
    width: '100%',
  },
  secondaryButton: {
    alignSelf: 'center',
  },
});
