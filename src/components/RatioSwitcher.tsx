import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View, ViewStyle } from 'react-native';
import { useTheme } from '../theme/ThemeContext';
import { TYPOGRAPHY } from '../theme/typography';
import { SPACING } from '../theme/spacing';
import { AspectRatioType } from '../data/types';

interface RatioOption {
  id: AspectRatioType;
  label: string;
  sublabel: string;
}

const OPTIONS: RatioOption[] = [
  { id: '9:19.5', label: 'Lock Screen', sublabel: '9:19.5' },
  { id: '9:16', label: 'Story', sublabel: '9:16' },
  { id: '1:1', label: 'Square', sublabel: '1:1' },
];

interface RatioSwitcherProps {
  selectedRatio: AspectRatioType;
  onSelectRatio: (ratio: AspectRatioType) => void;
  style?: ViewStyle;
}

export const RatioSwitcher: React.FC<RatioSwitcherProps> = ({
  selectedRatio,
  onSelectRatio,
  style,
}) => {
  const { colors } = useTheme();

  return (
    <View style={[styles.container, { borderColor: colors.border }, style]}>
      {OPTIONS.map((option) => {
        const isSelected = selectedRatio === option.id;
        return (
          <TouchableOpacity
            key={option.id}
            activeOpacity={0.8}
            onPress={() => onSelectRatio(option.id)}
            style={[
              styles.optionButton,
              {
                backgroundColor: isSelected ? colors.chipSelectedBg : 'transparent',
              },
            ]}
          >
            <Text
              style={[
                styles.optionLabel,
                {
                  color: isSelected ? colors.chipSelectedText : colors.textPrimary,
                  fontFamily: isSelected ? TYPOGRAPHY.fontBodySemiBold : TYPOGRAPHY.fontBodyMedium,
                },
              ]}
            >
              {option.label}
            </Text>
            <Text
              style={[
                styles.optionSublabel,
                {
                  color: isSelected ? 'rgba(28, 26, 23, 0.7)' : colors.textMuted,
                },
              ]}
            >
              {option.sublabel}
            </Text>
          </TouchableOpacity>
        );
      })}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    borderRadius: SPACING.cardRadius,
    borderWidth: 1,
    padding: 4,
    marginVertical: SPACING.md,
  },
  optionButton: {
    flex: 1,
    minHeight: SPACING.minTouchTarget,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: SPACING.cardRadius - 4,
    paddingVertical: 6,
  },
  optionLabel: {
    fontSize: TYPOGRAPHY.sizes.sm,
  },
  optionSublabel: {
    fontFamily: TYPOGRAPHY.fontBody,
    fontSize: TYPOGRAPHY.sizes.xs,
    marginTop: 2,
  },
});
