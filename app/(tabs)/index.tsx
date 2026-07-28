import React, { useState, useCallback } from 'react';
import { StyleSheet, View, FlatList, Text, TouchableOpacity } from 'react-native';
import { useRouter, useFocusEffect } from 'expo-router';
import { Feather } from '@expo/vector-icons';
import { useTheme } from '../../src/theme/ThemeContext';
import { TYPOGRAPHY } from '../../src/theme/typography';
import { SPACING } from '../../src/theme/spacing';
import { ManuscriptCard } from '../../src/components/ManuscriptCard';
import { Affirmation } from '../../src/data/types';
import { Storage } from '../../src/data/storage';

export default function HomeScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const [affirmations, setAffirmations] = useState<Affirmation[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(async () => {
    setLoading(true);
    const firstOpen = await Storage.isFirstOpen();
    if (firstOpen) {
      router.replace('/first-open');
      return;
    }

    const saved = await Storage.getSavedAffirmations();
    setAffirmations(saved);
    setLoading(false);
  }, []);

  useFocusEffect(
    useCallback(() => {
      loadData();
    }, [loadData])
  );

  const renderItem = ({ item }: { item: Affirmation }) => (
    <ManuscriptCard
      affirmation={item}
      size="standard"
      align="left"
      onPress={() => router.push({ pathname: '/detail', params: { id: item.id } })}
      style={styles.cardItem}
    />
  );

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={styles.headerBar}>
        <Text style={[styles.sectionHeading, { color: colors.textSecondary }]}>
          Your Saved Collection ({affirmations.length})
        </Text>
        <TouchableOpacity
          activeOpacity={0.7}
          onPress={() => router.push('/add')}
          style={[styles.addButton, { backgroundColor: colors.accent }]}
        >
          <Feather name="plus" size={20} color={colors.chipSelectedText} />
        </TouchableOpacity>
      </View>

      <FlatList
        data={affirmations}
        renderItem={renderItem}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  headerBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: SPACING.lg,
    paddingTop: SPACING.md,
    paddingBottom: SPACING.xs,
  },
  sectionHeading: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.sm,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  addButton: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
  listContent: {
    paddingHorizontal: SPACING.lg,
    paddingBottom: SPACING.xl,
    paddingTop: SPACING.sm,
  },
  cardItem: {
    marginBottom: SPACING.md,
  },
});
