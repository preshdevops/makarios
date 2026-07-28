import React, { useState, useEffect } from 'react';
import { StyleSheet, View, ScrollView, Alert, Share } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { Feather } from '@expo/vector-icons';
import { useTheme } from '../src/theme/ThemeContext';
import { TYPOGRAPHY } from '../src/theme/typography';
import { SPACING } from '../src/theme/spacing';
import { ManuscriptCard } from '../src/components/ManuscriptCard';
import { Button } from '../src/components/Button';
import { Affirmation } from '../src/data/types';
import { CURATED_AFFIRMATIONS } from '../src/data/curatedAffirmations';
import { Storage } from '../src/data/storage';

export default function AffirmationDetailScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const { id } = useLocalSearchParams<{ id: string }>();

  const [affirmation, setAffirmation] = useState<Affirmation | null>(null);
  const [isSaved, setIsSaved] = useState(false);

  useEffect(() => {
    if (!id) return;
    loadAffirmation();
  }, [id]);

  const loadAffirmation = async () => {
    const savedList = await Storage.getSavedAffirmations();
    const foundInSaved = savedList.find((item) => item.id === id);

    if (foundInSaved) {
      setAffirmation(foundInSaved);
      setIsSaved(true);
    } else {
      const foundInCurated = CURATED_AFFIRMATIONS.find((item) => item.id === id);
      if (foundInCurated) {
        setAffirmation(foundInCurated);
        setIsSaved(false);
      }
    }
  };

  if (!affirmation) {
    return null;
  }

  const handleToggleSave = async () => {
    if (isSaved) {
      // Remove from saved
      await Storage.deleteAffirmation(affirmation.id);
      setIsSaved(false);
    } else {
      // Add to saved
      await Storage.saveAffirmation({ ...affirmation, isFavorite: true });
      setIsSaved(true);
    }
  };

  const handleOpenImageGenerator = () => {
    router.push({
      pathname: '/image-generator',
      params: { id: affirmation.id, text: affirmation.text, reference: affirmation.reference || '' },
    });
  };

  const handleShareText = async () => {
    try {
      const message = `${affirmation.text}${affirmation.reference ? ` — ${affirmation.reference}` : ''}\n\n— via Makarios Affirmations`;
      await Share.share({ message });
    } catch (e) {
      console.error(e);
    }
  };

  const handleDelete = () => {
    Alert.alert(
      'Delete Affirmation',
      'Are you sure you want to remove this affirmation from your collection?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            await Storage.deleteAffirmation(affirmation.id);
            router.back();
          },
        },
      ]
    );
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.cardContainer}>
          <ManuscriptCard
            affirmation={affirmation}
            size="large"
            align="center"
            showTags={true}
          />
        </View>

        <View style={styles.actionsContainer}>
          <Button
            title={isSaved ? 'Saved to Mine ✓' : 'Save to Mine'}
            onPress={handleToggleSave}
            variant={isSaved ? 'secondary' : 'primary'}
            style={styles.actionButton}
          />

          <Button
            title="Create Wallpaper & Story Image"
            onPress={handleOpenImageGenerator}
            variant="primary"
            style={styles.actionButton}
          />

          <Button
            title="Add to Widget"
            onPress={() => router.push('/widget-config')}
            variant="secondary"
            style={styles.actionButton}
          />

          <Button
            title="Share Affirmation"
            onPress={handleShareText}
            variant="secondary"
            style={styles.actionButton}
          />

          {!affirmation.isCurated && isSaved ? (
            <Button
              title="Delete Affirmation"
              onPress={handleDelete}
              variant="danger"
              style={StyleSheet.flatten([styles.actionButton, { marginTop: SPACING.md }])}
            />
          ) : null}
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
    paddingBottom: SPACING.xxl,
  },
  cardContainer: {
    marginVertical: SPACING.md,
  },
  actionsContainer: {
    gap: SPACING.sm,
    marginTop: SPACING.md,
  },
  actionButton: {
    width: '100%',
  },
});
