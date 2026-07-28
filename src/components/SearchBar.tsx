import React from 'react';
import { StyleSheet, TextInput, View, ViewStyle, TouchableOpacity } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { useTheme } from '../theme/ThemeContext';
import { TYPOGRAPHY } from '../theme/typography';
import { SPACING } from '../theme/spacing';

interface SearchBarProps {
  value: string;
  onChangeText: (text: string) => void;
  placeholder?: string;
  onClear?: () => void;
  style?: ViewStyle;
}

export const SearchBar: React.FC<SearchBarProps> = ({
  value,
  onChangeText,
  placeholder = 'Search affirmations or scriptures...',
  onClear,
  style,
}) => {
  const { colors } = useTheme();

  return (
    <View
      style={[
        styles.container,
        {
          backgroundColor: colors.inputBg,
          borderColor: colors.border,
        },
        style,
      ]}
    >
      <Feather name="search" size={18} color={colors.accent} style={styles.icon} />
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.textMuted}
        style={[
          styles.input,
          {
            color: colors.textPrimary,
          },
        ]}
      />
      {value.length > 0 ? (
        <TouchableOpacity
          onPress={() => {
            onChangeText('');
            if (onClear) onClear();
          }}
          style={styles.clearButton}
        >
          <Feather name="x" size={16} color={colors.textMuted} />
        </TouchableOpacity>
      ) : null}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    minHeight: SPACING.minTouchTarget,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: SPACING.md,
    borderRadius: SPACING.cardRadius,
    borderWidth: 1,
    marginVertical: SPACING.sm,
  },
  icon: {
    marginRight: SPACING.sm,
  },
  input: {
    flex: 1,
    fontFamily: TYPOGRAPHY.fontBody,
    fontSize: TYPOGRAPHY.sizes.md,
    paddingVertical: 10,
  },
  clearButton: {
    padding: SPACING.xs,
  },
});
