import React from 'react';
import { StyleSheet, Text, TouchableOpacity, ViewStyle } from 'react-native';
import { useTheme } from '../theme/ThemeContext';
import { TYPOGRAPHY } from '../theme/typography';
import { SPACING } from '../theme/spacing';

interface TagChipProps {
  label: string;
  selected?: boolean;
  onPress: () => void;
  style?: ViewStyle;
}

export const TagChip: React.FC<TagChipProps> = ({
  label,
  selected = false,
  onPress,
  style,
}) => {
  const { colors } = useTheme();

  return (
    <TouchableOpacity
      activeOpacity={0.7}
      onPress={onPress}
      style={[
        styles.chip,
        {
          backgroundColor: selected ? colors.chipSelectedBg : colors.chipBg,
          borderColor: selected ? colors.accent : colors.border,
        },
        style,
      ]}
    >
      <Text
        style={[
          styles.label,
          {
            color: selected ? colors.chipSelectedText : colors.textPrimary,
            fontFamily: selected ? TYPOGRAPHY.fontBodySemiBold : TYPOGRAPHY.fontBodyMedium,
          },
        ]}
      >
        {label}
      </Text>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  chip: {
    minHeight: SPACING.minTouchTarget,
    paddingHorizontal: SPACING.md,
    paddingVertical: SPACING.sm,
    borderRadius: 22,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: SPACING.sm,
    marginBottom: SPACING.sm,
  },
  label: {
    fontSize: TYPOGRAPHY.sizes.sm,
    letterSpacing: 0.2,
  },
});
