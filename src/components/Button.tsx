import React from 'react';
import { StyleSheet, Text, TouchableOpacity, ViewStyle, TextStyle } from 'react-native';
import { useTheme } from '../theme/ThemeContext';
import { TYPOGRAPHY } from '../theme/typography';
import { SPACING } from '../theme/spacing';

interface ButtonProps {
  title: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'text' | 'danger';
  style?: ViewStyle;
  textStyle?: TextStyle;
  disabled?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  title,
  onPress,
  variant = 'primary',
  style,
  textStyle,
  disabled = false,
}) => {
  const { colors } = useTheme();

  if (variant === 'text') {
    return (
      <TouchableOpacity
        activeOpacity={0.7}
        onPress={onPress}
        disabled={disabled}
        style={[styles.textButton, style]}
      >
        <Text
          style={[
            styles.textButtonLabel,
            { color: colors.accentText },
            textStyle,
          ]}
        >
          {title}
        </Text>
      </TouchableOpacity>
    );
  }

  let backgroundColor = colors.accent;
  let textColor = colors.chipSelectedText;
  let borderColor = colors.accent;

  if (variant === 'secondary') {
    backgroundColor = 'transparent';
    textColor = colors.textPrimary;
    borderColor = colors.border;
  } else if (variant === 'danger') {
    backgroundColor = 'transparent';
    textColor = colors.error;
    borderColor = colors.error;
  }

  return (
    <TouchableOpacity
      activeOpacity={0.8}
      onPress={onPress}
      disabled={disabled}
      style={[
        styles.button,
        {
          backgroundColor,
          borderColor,
          opacity: disabled ? 0.5 : 1,
        },
        style,
      ]}
    >
      <Text
        style={[
          styles.buttonLabel,
          { color: textColor },
          textStyle,
        ]}
      >
        {title}
      </Text>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  button: {
    minHeight: SPACING.minTouchTarget,
    paddingHorizontal: SPACING.lg,
    paddingVertical: 12,
    borderRadius: SPACING.cardRadius,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
  },
  buttonLabel: {
    fontFamily: TYPOGRAPHY.fontBodySemiBold,
    fontSize: TYPOGRAPHY.sizes.md,
    letterSpacing: 0.3,
  },
  textButton: {
    minHeight: SPACING.minTouchTarget,
    paddingHorizontal: SPACING.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
  textButtonLabel: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.md,
    textDecorationLine: 'underline',
  },
});
