import React, { useState } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TextInput,
  ScrollView,
  TouchableOpacity,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { useRouter } from 'expo-router';
import { Feather } from '@expo/vector-icons';
import { useTheme } from '../src/theme/ThemeContext';
import { TYPOGRAPHY } from '../src/theme/typography';
import { SPACING } from '../src/theme/spacing';
import { Button } from '../src/components/Button';
import { TagChip } from '../src/components/TagChip';
import { ALL_TAGS } from '../src/data/curatedAffirmations';
import { Storage } from '../src/data/storage';

export default function AddAffirmationScreen() {
  const { colors } = useTheme();
  const router = useRouter();

  const [text, setText] = useState('');
  const [reference, setReference] = useState('');
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [tagsExpanded, setTagsExpanded] = useState(false);

  const toggleTag = (tag: string) => {
    if (selectedTags.includes(tag)) {
      setSelectedTags(selectedTags.filter((t) => t !== tag));
    } else {
      setSelectedTags([...selectedTags, tag]);
    }
  };

  const handleSave = async () => {
    if (!text.trim()) return;

    await Storage.addAffirmation({
      text: text.trim(),
      reference: reference.trim() || undefined,
      tags: selectedTags,
    });

    router.back();
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      style={[styles.container, { backgroundColor: colors.background }]}
    >
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps="handled"
      >
        <Text style={[styles.sectionLabel, { color: colors.textSecondary }]}>
          MANUSCRIPT CARD DRAFT
        </Text>

        {/* WYSIWYG Manuscript Card Input */}
        <View
          style={[
            styles.cardContainer,
            {
              backgroundColor: colors.cardBg,
              borderColor: colors.cardBorder,
            },
          ]}
        >
          <TextInput
            multiline
            value={text}
            onChangeText={setText}
            placeholder="Write your affirmation here... Rooted in God's promises."
            placeholderTextColor={colors.textMuted}
            style={[
              styles.wysiwygInput,
              {
                color: colors.textPrimary,
                fontFamily: TYPOGRAPHY.fontDisplay,
              },
            ]}
          />

          <TextInput
            value={reference}
            onChangeText={setReference}
            placeholder="Reference e.g. Matthew 5:3 (optional)"
            placeholderTextColor={colors.textMuted}
            style={[
              styles.referenceInput,
              {
                color: colors.accentText,
                fontFamily: TYPOGRAPHY.fontDisplayItalic,
              },
            ]}
          />

          {selectedTags.length > 0 ? (
            <View style={styles.selectedTagsRow}>
              {selectedTags.map((tag) => (
                <View
                  key={tag}
                  style={[
                    styles.miniChip,
                    { backgroundColor: colors.chipBg, borderColor: colors.border },
                  ]}
                >
                  <Text style={[styles.miniChipText, { color: colors.textSecondary }]}>
                    {tag}
                  </Text>
                </View>
              ))}
            </View>
          ) : null}
        </View>

        {/* Collapsible Tag Selector */}
        <TouchableOpacity
          activeOpacity={0.7}
          onPress={() => setTagsExpanded(!tagsExpanded)}
          style={[styles.tagHeader, { borderColor: colors.border }]}
        >
          <View style={styles.tagHeaderLeft}>
            <Feather name="tag" size={16} color={colors.accent} />
            <Text style={[styles.tagHeaderText, { color: colors.textPrimary }]}>
              {selectedTags.length === 0
                ? 'Add Tags (Optional)'
                : `Tags (${selectedTags.length} selected)`}
            </Text>
          </View>
          <Feather
            name={tagsExpanded ? 'chevron-up' : 'chevron-down'}
            size={18}
            color={colors.textMuted}
          />
        </TouchableOpacity>

        {tagsExpanded ? (
          <View style={styles.tagChipsContainer}>
            {ALL_TAGS.map((tag) => (
              <TagChip
                key={tag}
                label={tag}
                selected={selectedTags.includes(tag)}
                onPress={() => toggleTag(tag)}
              />
            ))}
          </View>
        ) : null}

        <View style={styles.buttonWrapper}>
          <Button
            title="Save Affirmation"
            onPress={handleSave}
            disabled={!text.trim()}
            variant="primary"
          />
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    padding: SPACING.lg,
  },
  sectionLabel: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.xs,
    letterSpacing: 0.8,
    marginBottom: SPACING.xs,
  },
  cardContainer: {
    borderRadius: SPACING.cardRadius,
    borderWidth: SPACING.cardBorderWidth,
    padding: SPACING.cardPadding,
    minHeight: 200,
    marginVertical: SPACING.sm,
  },
  wysiwygInput: {
    fontSize: TYPOGRAPHY.sizes.affirmationMd,
    lineHeight: Math.round(TYPOGRAPHY.sizes.affirmationMd * TYPOGRAPHY.lineHeights.affirmation),
    minHeight: 120,
    textAlignVertical: 'top',
  },
  referenceInput: {
    fontSize: TYPOGRAPHY.sizes.sm,
    marginTop: SPACING.md,
  },
  selectedTagsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: SPACING.md,
    gap: 6,
  },
  miniChip: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
    borderWidth: 1,
  },
  miniChipText: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.xs,
  },
  tagHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: SPACING.md,
    borderBottomWidth: 1,
    marginTop: SPACING.md,
  },
  tagHeaderLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  tagHeaderText: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.md,
  },
  tagChipsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: SPACING.md,
  },
  buttonWrapper: {
    marginTop: SPACING.xl,
    marginBottom: SPACING.lg,
  },
});
