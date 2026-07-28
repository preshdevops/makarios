import React, { useState, useRef, useEffect } from 'react';
import {
  StyleSheet,
  Text,
  View,
  ScrollView,
  Animated,
  Dimensions,
  Alert,
} from 'react-native';
import { useLocalSearchParams } from 'expo-router';
import ViewShot, { captureRef } from 'react-native-view-shot';
import * as Sharing from 'expo-sharing';
import * as MediaLibrary from 'expo-media-library';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme } from '../src/theme/ThemeContext';
import { TYPOGRAPHY } from '../src/theme/typography';
import { SPACING } from '../src/theme/spacing';
import { RatioSwitcher } from '../src/components/RatioSwitcher';
import { Button } from '../src/components/Button';
import { AspectRatioType } from '../src/data/types';
import { PALETTE } from '../src/theme/colors';

const { width: SCREEN_WIDTH } = Dimensions.get('window');
const PREVIEW_WIDTH = SCREEN_WIDTH - SPACING.lg * 2;

export default function ImageGeneratorScreen() {
  const { colors } = useTheme();
  const params = useLocalSearchParams<{ id?: string; text?: string; reference?: string }>();

  const affirmationText = params.text || 'Blessed are the poor in spirit, for theirs is the kingdom of heaven.';
  const affirmationRef = params.reference || 'Matthew 5:3';

  const [selectedRatio, setSelectedRatio] = useState<AspectRatioType>('9:19.5');
  const [capturing, setCapturing] = useState(false);

  const fadeAnim = useRef(new Animated.Value(1)).current;
  const viewShotRef = useRef<any>(null);

  const getAspectRatioNumber = (ratio: AspectRatioType) => {
    switch (ratio) {
      case '9:19.5':
        return 9 / 19.5;
      case '9:16':
        return 9 / 16;
      case '1:1':
        return 1;
      default:
        return 9 / 19.5;
    }
  };

  const handleRatioChange = (newRatio: AspectRatioType) => {
    if (newRatio === selectedRatio) return;
    // Crossfade animation
    Animated.timing(fadeAnim, {
      toValue: 0.2,
      duration: 100,
      useNativeDriver: true,
    }).start(() => {
      setSelectedRatio(newRatio);
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 150,
        useNativeDriver: true,
      }).start();
    });
  };

  const captureImage = async (): Promise<string | null> => {
    try {
      setCapturing(true);
      const uri = await captureRef(viewShotRef, {
        format: 'png',
        quality: 1.0,
        result: 'tmpfile',
      });
      return uri;
    } catch (error) {
      console.error('Failed to capture image', error);
      Alert.alert('Export Error', 'Could not generate image. Please try again.');
      return null;
    } finally {
      setCapturing(false);
    }
  };

  const handleSaveToPhotos = async () => {
    const uri = await captureImage();
    if (!uri) return;

    try {
      const { status } = await MediaLibrary.requestPermissionsAsync();
      if (status === 'granted') {
        await MediaLibrary.saveToLibraryAsync(uri);
        Alert.alert('Saved!', 'Image saved to your photos library.');
      } else {
        // Fallback to sharing if permission not granted
        await Sharing.shareAsync(uri);
      }
    } catch (e) {
      console.error('Error saving image:', e);
      // Fallback share
      await Sharing.shareAsync(uri);
    }
  };

  const handleShareSheet = async () => {
    const uri = await captureImage();
    if (!uri) return;

    try {
      const isAvailable = await Sharing.isAvailableAsync();
      if (isAvailable) {
        await Sharing.shareAsync(uri, {
          dialogTitle: 'Share Makarios Affirmation',
          mimeType: 'image/png',
        });
      } else {
        Alert.alert('Share Unavailable', 'Sharing is not supported on this device.');
      }
    } catch (e) {
      console.error('Error sharing image:', e);
    }
  };

  const aspectRatio = getAspectRatioNumber(selectedRatio);
  const previewHeight = Math.min(PREVIEW_WIDTH / aspectRatio, 480);

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <Text style={[styles.instruction, { color: colors.textSecondary }]}>
          Select format & export for your device or stories
        </Text>

        <RatioSwitcher
          selectedRatio={selectedRatio}
          onSelectRatio={handleRatioChange}
        />

        {/* Live Canvas Preview */}
        <Animated.View style={[styles.previewWrapper, { opacity: fadeAnim }]}>
          <ViewShot
            ref={viewShotRef}
            options={{ format: 'png', quality: 1.0 }}
            style={{ width: PREVIEW_WIDTH, height: previewHeight }}
          >
            {/* Thematic Soft Dawn Gradient Background inside output image only */}
            <LinearGradient
              colors={[PALETTE.parchment, '#EBD8B8', PALETTE.goldLeaf]}
              start={{ x: 0, y: 0 }}
              end={{ x: 1, y: 1 }}
              style={styles.canvasBackground}
            >
              {/* Manuscript Card Overlay Inside Output */}
              <View style={styles.cardContainer}>
                <Text style={styles.cardText}>{affirmationText}</Text>
                {affirmationRef ? (
                  <Text style={styles.cardRef}>— {affirmationRef}</Text>
                ) : null}
              </View>

              {/* Gold Leaf Wordmark in Bottom-Right Corner */}
              <Text style={styles.wordmark}>MAKARIOS</Text>
            </LinearGradient>
          </ViewShot>
        </Animated.View>

        {/* Export Action Controls */}
        <View style={styles.actionRow}>
          <Button
            title={capturing ? 'Generating...' : 'Save to Camera Roll'}
            onPress={handleSaveToPhotos}
            disabled={capturing}
            variant="primary"
            style={styles.actionBtn}
          />
          <Button
            title="Share Image"
            onPress={handleShareSheet}
            disabled={capturing}
            variant="secondary"
            style={styles.actionBtn}
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
  instruction: {
    fontFamily: TYPOGRAPHY.fontBodyMedium,
    fontSize: TYPOGRAPHY.sizes.sm,
    textAlign: 'center',
    marginBottom: SPACING.xs,
  },
  previewWrapper: {
    marginVertical: SPACING.md,
    borderRadius: SPACING.cardRadius,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: 'rgba(28, 26, 23, 0.15)',
  },
  canvasBackground: {
    flex: 1,
    padding: SPACING.lg,
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
  },
  cardContainer: {
    width: '85%',
    backgroundColor: PALETTE.parchment,
    borderRadius: SPACING.cardRadius,
    borderWidth: 1,
    borderColor: 'rgba(28, 26, 23, 0.2)',
    padding: SPACING.lg,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cardText: {
    fontFamily: TYPOGRAPHY.fontDisplay,
    fontSize: 22,
    lineHeight: 32,
    color: PALETTE.ink,
    textAlign: 'center',
  },
  cardRef: {
    fontFamily: TYPOGRAPHY.fontDisplayItalic,
    fontSize: 14,
    color: PALETTE.goldDark,
    marginTop: 10,
    textAlign: 'center',
  },
  wordmark: {
    position: 'absolute',
    bottom: 12,
    right: 16,
    fontFamily: TYPOGRAPHY.fontDisplayBold,
    fontSize: 10,
    letterSpacing: 2,
    color: PALETTE.goldDark,
    opacity: 0.85,
  },
  actionRow: {
    width: '100%',
    gap: SPACING.sm,
    marginTop: SPACING.md,
    marginBottom: SPACING.lg,
  },
  actionBtn: {
    width: '100%',
  },
});
