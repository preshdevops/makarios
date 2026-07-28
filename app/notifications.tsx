import React, { useState, useEffect } from 'react';
import { StyleSheet, Text, View, ScrollView, TouchableOpacity } from 'react-native';
import { useTheme } from '../src/theme/ThemeContext';
import { TYPOGRAPHY } from '../src/theme/typography';
import { SPACING } from '../src/theme/spacing';
import { SettingsRow } from '../src/components/SettingsRow';
import { TagChip } from '../src/components/TagChip';
import { ALL_TAGS } from '../src/data/curatedAffirmations';
import { NotificationSettings } from '../src/data/types';
import { Storage, DEFAULT_NOTIFICATIONS } from '../src/data/storage';

export default function NotificationsScreen() {
  const { colors } = useTheme();
  const [settings, setSettings] = useState<NotificationSettings>(DEFAULT_NOTIFICATIONS);

  useEffect(() => {
    Storage.getNotificationSettings().then(setSettings);
  }, []);

  const updateSetting = <K extends keyof NotificationSettings>(key: K, value: NotificationSettings[K]) => {
    const updated = { ...settings, [key]: value };
    setSettings(updated);
    Storage.saveNotificationSettings(updated);
  };

  const handleFrequencyCycle = () => {
    const freqs: NotificationSettings['frequency'][] = ['3h', '6h', 'daily'];
    const currentIndex = freqs.indexOf(settings.frequency);
    const nextIndex = (currentIndex + 1) % freqs.length;
    updateSetting('frequency', freqs[nextIndex]);
  };

  const handleSourceCycle = () => {
    if (settings.source === 'all') {
      updateSetting('source', 'favorites');
    } else if (settings.source === 'favorites') {
      updateSetting('source', ['Identity', 'Peace']);
    } else {
      updateSetting('source', 'all');
    }
  };

  const formatSourceLabel = () => {
    if (settings.source === 'all') return 'All Affirmations';
    if (settings.source === 'favorites') return 'Favorites Only';
    if (Array.isArray(settings.source)) return `${settings.source.length} Tags`;
    return 'All Affirmations';
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>
          SCHEDULED NOTIFICATIONS
        </Text>

        <View style={[styles.cardGroup, { borderColor: colors.border, backgroundColor: colors.surface }]}>
          <SettingsRow
            label="Enable Daily Affirmations"
            subtitle="Receive Beatitudes directly on your lock screen"
            type="switch"
            value={settings.enabled}
            onValueChange={(val) => updateSetting('enabled', val)}
          />

          {settings.enabled ? (
            <>
              <SettingsRow
                label="Delivery Interval"
                subtitle="How often new affirmations appear"
                type="value"
                value={settings.frequency === '3h' ? 'Every 3 Hours' : settings.frequency === '6h' ? 'Every 6 Hours' : 'Once Daily'}
                onPress={handleFrequencyCycle}
              />

              <SettingsRow
                label="Affirmation Source"
                subtitle="Which collection to draw from"
                type="value"
                value={formatSourceLabel()}
                onPress={handleSourceCycle}
              />

              <SettingsRow
                label="Quiet Hours Start"
                subtitle="No notifications after this time"
                type="value"
                value={settings.quietStart}
                onPress={() => updateSetting('quietStart', settings.quietStart === '22:00' ? '23:00' : '22:00')}
              />

              <SettingsRow
                label="Quiet Hours End"
                subtitle="Notifications resume in the morning"
                type="value"
                value={settings.quietEnd}
                onPress={() => updateSetting('quietEnd', settings.quietEnd === '07:00' ? '08:00' : '07:00')}
              />
            </>
          ) : null}
        </View>

        {settings.enabled && Array.isArray(settings.source) ? (
          <View style={styles.tagSection}>
            <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>
              SELECT TAGS FOR NOTIFICATIONS
            </Text>
            <View style={styles.tagChips}>
              {ALL_TAGS.map((tag) => {
                const isSelected = (settings.source as string[]).includes(tag);
                return (
                  <TagChip
                    key={tag}
                    label={tag}
                    selected={isSelected}
                    onPress={() => {
                      const current = settings.source as string[];
                      const updated = isSelected
                        ? current.filter((t) => t !== tag)
                        : [...current, tag];
                      updateSetting('source', updated.length === 0 ? 'all' : updated);
                    }}
                  />
                );
              })}
            </View>
          </View>
        ) : null}
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
  tagSection: {
    marginTop: SPACING.md,
  },
  tagChips: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: SPACING.xs,
  },
});
