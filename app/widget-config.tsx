import React, { useState, useEffect } from 'react';
import { StyleSheet, Text, View, ScrollView, TouchableOpacity } from 'react-native';
import { useTheme } from '../src/theme/ThemeContext';
import { TYPOGRAPHY } from '../src/theme/typography';
import { SPACING } from '../src/theme/spacing';
import { SettingsRow } from '../src/components/SettingsRow';
import { WidgetConfig } from '../src/data/types';
import { Storage, DEFAULT_WIDGET } from '../src/data/storage';
import { STARTER_AFFIRMATION } from '../src/data/curatedAffirmations';

export default function WidgetConfigScreen() {
  const { colors } = useTheme();
  const [config, setConfig] = useState<WidgetConfig>(DEFAULT_WIDGET);

  useEffect(() => {
    Storage.getWidgetConfig().then(setConfig);
  }, []);

  const updateConfig = <K extends keyof WidgetConfig>(key: K, value: WidgetConfig[K]) => {
    const updated = { ...config, [key]: value };
    setConfig(updated);
    Storage.saveWidgetConfig(updated);
  };

  const handleSizeChange = (size: WidgetConfig['size']) => {
    updateConfig('size', size);
  };

  const handleSourceCycle = () => {
    if (config.source === 'all') {
      updateConfig('source', 'favorites');
    } else if (config.source === 'favorites') {
      updateConfig('source', ['Peace']);
    } else {
      updateConfig('source', 'all');
    }
  };

  const formatSourceLabel = () => {
    if (config.source === 'all') return 'All Affirmations';
    if (config.source === 'favorites') return 'Favorites Only';
    if (Array.isArray(config.source)) return `${config.source.join(', ')}`;
    return 'All Affirmations';
  };

  const getPreviewDimensions = () => {
    switch (config.size) {
      case 'small':
        return { width: 150, height: 150, fontSize: 15, padding: 14 };
      case 'medium':
        return { width: 300, height: 150, fontSize: 18, padding: 18 };
      case 'large':
        return { width: 300, height: 300, fontSize: 22, padding: 24 };
    }
  };

  const dims = getPreviewDimensions();

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>
          HOME-SCREEN WIDGET PREVIEW
        </Text>

        {/* Live Widget Preview Box */}
        <View style={styles.previewContainer}>
          <View
            style={[
              styles.widgetCard,
              {
                width: dims.width,
                height: dims.height,
                padding: dims.padding,
                backgroundColor: colors.cardBg,
                borderColor: colors.cardBorder,
              },
            ]}
          >
            <Text
              numberOfLines={config.size === 'small' ? 4 : 6}
              style={[
                styles.widgetText,
                {
                  color: colors.textPrimary,
                  fontSize: dims.fontSize,
                  lineHeight: Math.round(dims.fontSize * 1.4),
                },
              ]}
            >
              {STARTER_AFFIRMATION.text}
            </Text>
            <Text style={[styles.widgetRef, { color: colors.accentText }]}>
              {STARTER_AFFIRMATION.reference}
            </Text>
          </View>
        </View>

        {/* Size Selection Tabs */}
        <View style={[styles.sizeSelector, { borderColor: colors.border }]}>
          {(['small', 'medium', 'large'] as WidgetConfig['size'][]).map((size) => {
            const isSelected = config.size === size;
            return (
              <TouchableOpacity
                key={size}
                activeOpacity={0.8}
                onPress={() => handleSizeChange(size)}
                style={[
                  styles.sizeOption,
                  {
                    backgroundColor: isSelected ? colors.chipSelectedBg : 'transparent',
                  },
                ]}
              >
                <Text
                  style={[
                    styles.sizeText,
                    {
                      color: isSelected ? colors.chipSelectedText : colors.textPrimary,
                      fontFamily: isSelected ? TYPOGRAPHY.fontBodySemiBold : TYPOGRAPHY.fontBodyMedium,
                    },
                  ]}
                >
                  {size.toUpperCase()}
                </Text>
              </TouchableOpacity>
            );
          })}
        </View>

        <Text style={[styles.sectionTitle, { color: colors.textSecondary }]}>
          WIDGET CONFIGURATION
        </Text>

        <View style={[styles.cardGroup, { borderColor: colors.border, backgroundColor: colors.surface }]}>
          <SettingsRow
            label="Affirmation Source"
            subtitle="Content shown on widget"
            type="value"
            value={formatSourceLabel()}
            onPress={handleSourceCycle}
          />

          <SettingsRow
            label="Refresh Frequency"
            subtitle="Updates automatically on your home screen"
            type="value"
            value={config.refreshMinutes === 360 ? 'Every 6 Hours' : 'Daily'}
            onPress={() => updateConfig('refreshMinutes', config.refreshMinutes === 360 ? 1440 : 360)}
          />
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
    alignItems: 'center',
  },
  sectionTitle: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.xs,
    letterSpacing: 0.8,
    alignSelf: 'flex-start',
    marginBottom: SPACING.xs,
    marginTop: SPACING.md,
  },
  previewContainer: {
    minHeight: 320,
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
    marginVertical: SPACING.sm,
  },
  widgetCard: {
    borderRadius: SPACING.cardRadius,
    borderWidth: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  widgetText: {
    fontFamily: TYPOGRAPHY.fontDisplay,
    textAlign: 'center',
  },
  widgetRef: {
    fontFamily: TYPOGRAPHY.fontDisplayItalic,
    fontSize: 11,
    marginTop: 6,
  },
  sizeSelector: {
    flexDirection: 'row',
    width: '100%',
    borderRadius: SPACING.cardRadius,
    borderWidth: 1,
    padding: 4,
    marginVertical: SPACING.md,
  },
  sizeOption: {
    flex: 1,
    minHeight: SPACING.minTouchTarget,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: SPACING.cardRadius - 4,
  },
  sizeText: {
    fontSize: TYPOGRAPHY.sizes.xs,
    letterSpacing: 0.5,
  },
  cardGroup: {
    width: '100%',
    borderRadius: SPACING.cardRadius,
    borderWidth: 1,
    overflow: 'hidden',
  },
});
