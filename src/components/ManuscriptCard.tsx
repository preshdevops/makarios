import React, { useEffect, useRef } from 'react';
import { StyleSheet, Text, View, Animated, TouchableOpacity, ViewStyle } from 'react-native';
import { useTheme } from '../theme/ThemeContext';
import { TYPOGRAPHY } from '../theme/typography';
import { SPACING } from '../theme/spacing';
import { Affirmation } from '../data/types';

interface ManuscriptCardProps {
  affirmation: Affirmation;
  size?: 'compact' | 'standard' | 'large' | 'preview';
  align?: 'center' | 'left';
  onPress?: () => void;
  style?: ViewStyle;
  showTags?: boolean;
  animated?: boolean;
}

export const ManuscriptCard: React.FC<ManuscriptCardProps> = ({
  affirmation,
  size = 'standard',
  align = 'center',
  onPress,
  style,
  showTags = true,
  animated = true,
}) => {
  const { colors } = useTheme();
  const fadeAnim = useRef(new Animated.Value(animated ? 0 : 1)).current;
  const slideAnim = useRef(new Animated.Value(animated ? 8 : 0)).current;

  useEffect(() => {
    if (animated) {
      Animated.parallel([
        Animated.timing(fadeAnim, {
          toValue: 1,
          duration: 200,
          useNativeDriver: true,
        }),
        Animated.timing(slideAnim, {
          toValue: 0,
          duration: 200,
          useNativeDriver: true,
        }),
      ]).start();
    }
  }, [animated]);

  const fontSizeMap = {
    compact: TYPOGRAPHY.sizes.affirmationSm, // 22
    standard: TYPOGRAPHY.sizes.affirmationMd, // 26
    large: TYPOGRAPHY.sizes.affirmationLg, // 30
    preview: TYPOGRAPHY.sizes.affirmationMd,
  };

  const currentFontSize = fontSizeMap[size] || TYPOGRAPHY.sizes.affirmationMd;
  const lineHeight = Math.round(currentFontSize * TYPOGRAPHY.lineHeights.affirmation);

  const ContainerComponent = onPress ? TouchableOpacity : View;

  return (
    <Animated.View
      style={[
        {
          opacity: fadeAnim,
          transform: [{ translateY: slideAnim }],
        },
      ]}
    >
      <ContainerComponent
        activeOpacity={onPress ? 0.85 : 1}
        onPress={onPress}
        style={[
          styles.card,
          {
            backgroundColor: colors.cardBg,
            borderColor: colors.cardBorder,
          },
          style,
        ]}
      >
        <Text
          style={[
            styles.affirmationText,
            {
              color: colors.textPrimary,
              fontSize: currentFontSize,
              lineHeight: lineHeight,
              textAlign: align,
            },
          ]}
        >
          {affirmation.text}
        </Text>

        {affirmation.reference ? (
          <Text
            style={[
              styles.referenceText,
              {
                color: colors.accentText,
                textAlign: align,
              },
            ]}
          >
            — {affirmation.reference}
          </Text>
        ) : null}

        {showTags && affirmation.tags && affirmation.tags.length > 0 ? (
          <View style={[styles.tagRow, { justifyContent: align === 'center' ? 'center' : 'flex-start' }]}>
            {affirmation.tags.map((tag) => (
              <View
                key={tag}
                style={[
                  styles.miniChip,
                  {
                    backgroundColor: colors.chipBg,
                    borderColor: colors.border,
                  },
                ]}
              >
                <Text style={[styles.miniChipText, { color: colors.textSecondary }]}>
                  {tag}
                </Text>
              </View>
            ))}
          </View>
        ) : null}
      </ContainerComponent>
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  card: {
    borderRadius: SPACING.cardRadius,
    borderWidth: SPACING.cardBorderWidth,
    padding: SPACING.cardPadding,
    minHeight: 140,
    justifyContent: 'center',
    marginVertical: SPACING.sm,
  },
  affirmationText: {
    fontFamily: TYPOGRAPHY.fontDisplay,
  },
  referenceText: {
    fontFamily: TYPOGRAPHY.fontDisplayItalic,
    fontSize: TYPOGRAPHY.sizes.sm,
    marginTop: SPACING.sm,
    letterSpacing: 0.5,
  },
  tagRow: {
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
    letterSpacing: 0.3,
  },
});
