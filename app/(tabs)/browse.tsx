import React, { useState, useMemo } from 'react';
import { StyleSheet, View, Text, ScrollView, FlatList } from 'react-native';
import { useRouter } from 'expo-router';
import { useTheme } from '../../src/theme/ThemeContext';
import { TYPOGRAPHY } from '../../src/theme/typography';
import { SPACING } from '../../src/theme/spacing';
import { SearchBar } from '../../src/components/SearchBar';
import { ManuscriptCard } from '../../src/components/ManuscriptCard';
import { ALL_TAGS, CURATED_AFFIRMATIONS } from '../../src/data/curatedAffirmations';
import { Affirmation } from '../../src/data/types';

export default function BrowseScreen() {
  const { colors } = useTheme();
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');

  // Filtered list when searching
  const searchResults = useMemo(() => {
    if (!searchQuery.trim()) return [];
    const q = searchQuery.toLowerCase();
    return CURATED_AFFIRMATIONS.filter(
      (item) =>
        item.text.toLowerCase().includes(q) ||
        (item.reference && item.reference.toLowerCase().includes(q)) ||
        item.tags.some((t) => t.toLowerCase().includes(q))
    );
  }, [searchQuery]);

  // Group affirmations by tag
  const taggedSections = useMemo(() => {
    return ALL_TAGS.map((tag) => {
      const items = CURATED_AFFIRMATIONS.filter((item) => item.tags.includes(tag));
      return { tag, items };
    }).filter((section) => section.items.length > 0);
  }, []);

  const handleCardPress = (id: string) => {
    router.push({ pathname: '/detail', params: { id } });
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View style={styles.searchHeader}>
        <SearchBar
          value={searchQuery}
          onChangeText={setSearchQuery}
          placeholder="Search library by word or verse..."
        />
      </View>

      {searchQuery.trim().length > 0 ? (
        <ScrollView contentContainerStyle={styles.searchList}>
          <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>
            Search Results ({searchResults.length})
          </Text>
          {searchResults.length === 0 ? (
            <Text style={[styles.emptyText, { color: colors.textMuted }]}>
              No affirmations found matching "{searchQuery}"
            </Text>
          ) : (
            searchResults.map((item) => (
              <ManuscriptCard
                key={item.id}
                affirmation={item}
                size="standard"
                align="left"
                onPress={() => handleCardPress(item.id)}
                style={styles.searchCard}
              />
            ))
          )}
        </ScrollView>
      ) : (
        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.scrollContent}
        >
          {taggedSections.map((section) => (
            <View key={section.tag} style={styles.sectionContainer}>
              <View style={styles.sectionHeader}>
                <Text style={[styles.tagTitle, { color: colors.textPrimary }]}>
                  {section.tag}
                </Text>
                <Text style={[styles.itemCount, { color: colors.accentText }]}>
                  {section.items.length} cards
                </Text>
              </View>

              <FlatList
                horizontal
                data={section.items}
                keyExtractor={(item) => item.id}
                showsHorizontalScrollIndicator={false}
                contentContainerStyle={styles.horizontalList}
                renderItem={({ item }) => (
                  <View style={styles.horizontalCardWrapper}>
                    <ManuscriptCard
                      affirmation={item}
                      size="compact"
                      align="left"
                      onPress={() => handleCardPress(item.id)}
                    />
                  </View>
                )}
              />
            </View>
          ))}
        </ScrollView>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  searchHeader: {
    paddingHorizontal: SPACING.lg,
    paddingTop: SPACING.xs,
    paddingBottom: SPACING.xs,
  },
  scrollContent: {
    paddingBottom: SPACING.xl,
  },
  sectionContainer: {
    marginTop: SPACING.md,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: SPACING.lg,
    marginBottom: SPACING.xs,
  },
  tagTitle: {
    fontFamily: TYPOGRAPHY.fontDisplaySemiBold,
    fontSize: TYPOGRAPHY.sizes.xl,
  },
  itemCount: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.xs,
    letterSpacing: 0.5,
  },
  horizontalList: {
    paddingHorizontal: SPACING.lg,
    paddingVertical: SPACING.xs,
  },
  horizontalCardWrapper: {
    width: 280,
    marginRight: SPACING.md,
  },
  searchList: {
    paddingHorizontal: SPACING.lg,
    paddingBottom: SPACING.xl,
  },
  sectionTitle: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.sm,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
    marginVertical: SPACING.sm,
  },
  searchCard: {
    marginBottom: SPACING.md,
  },
  emptyText: {
    fontFamily: TYPOGRAPHY.fontBody,
    fontSize: TYPOGRAPHY.sizes.md,
    marginTop: SPACING.lg,
    textAlign: 'center',
  },
});
