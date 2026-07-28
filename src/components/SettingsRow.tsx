import React from 'react';
import { StyleSheet, Text, View, Switch, TouchableOpacity, ViewStyle } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { useTheme } from '../theme/ThemeContext';
import { TYPOGRAPHY } from '../theme/typography';
import { SPACING } from '../theme/spacing';

interface SettingsRowProps {
  label: string;
  subtitle?: string;
  type?: 'switch' | 'link' | 'value' | 'custom';
  value?: boolean | string;
  onValueChange?: (val: any) => void;
  onPress?: () => void;
  rightComponent?: React.ReactNode;
  style?: ViewStyle;
}

export const SettingsRow: React.FC<SettingsRowProps> = ({
  label,
  subtitle,
  type = 'link',
  value,
  onValueChange,
  onPress,
  rightComponent,
  style,
}) => {
  const { colors } = useTheme();

  const Container = onPress ? TouchableOpacity : View;

  return (
    <Container
      activeOpacity={onPress ? 0.7 : 1}
      onPress={onPress}
      style={[
        styles.row,
        {
          borderColor: colors.border,
        },
        style,
      ]}
    >
      <View style={styles.textContainer}>
        <Text style={[styles.label, { color: colors.textPrimary }]}>{label}</Text>
        {subtitle ? (
          <Text style={[styles.subtitle, { color: colors.textMuted }]}>{subtitle}</Text>
        ) : null}
      </View>

      {type === 'switch' && typeof value === 'boolean' && onValueChange ? (
        <Switch
          value={value}
          onValueChange={onValueChange}
          trackColor={{ false: colors.chipBg, true: colors.accent }}
          thumbColor={colors.surface}
        />
      ) : null}

      {type === 'value' && typeof value === 'string' ? (
        <View style={styles.valueContainer}>
          <Text style={[styles.valueText, { color: colors.accentText }]}>{value}</Text>
          {onPress ? (
            <Feather name="chevron-right" size={18} color={colors.textMuted} style={styles.chevron} />
          ) : null}
        </View>
      ) : null}

      {type === 'link' && onPress ? (
        <Feather name="chevron-right" size={18} color={colors.textMuted} />
      ) : null}

      {type === 'custom' && rightComponent ? rightComponent : null}
    </Container>
  );
};

const styles = StyleSheet.create({
  row: {
    minHeight: SPACING.minTouchTarget + 8,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: SPACING.md,
    paddingHorizontal: SPACING.md,
    borderBottomWidth: 1,
  },
  textContainer: {
    flex: 1,
    paddingRight: SPACING.md,
  },
  label: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.md,
  },
  subtitle: {
    fontFamily: TYPOGRAPHY.fontBody,
    fontSize: TYPOGRAPHY.sizes.xs,
    marginTop: 2,
  },
  valueContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  valueText: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.sm,
  },
  chevron: {
    marginLeft: 4,
  },
});
