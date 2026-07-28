import AsyncStorage from '@react-native-async-storage/async-storage';
import { Affirmation, NotificationSettings, WidgetConfig } from './types';
import { CURATED_AFFIRMATIONS, STARTER_AFFIRMATION } from './curatedAffirmations';

const KEYS = {
  AFFIRMATIONS: '@makarios/user_affirmations',
  FIRST_OPEN: '@makarios/first_open_done',
  NOTIFICATIONS: '@makarios/notification_settings',
  WIDGET: '@makarios/widget_config',
};

export const DEFAULT_NOTIFICATIONS: NotificationSettings = {
  enabled: true,
  frequency: '6h',
  source: 'all',
  quietStart: '22:00',
  quietEnd: '07:00',
};

export const DEFAULT_WIDGET: WidgetConfig = {
  size: 'medium',
  source: 'all',
  refreshMinutes: 360,
};

export const Storage = {
  // First Open Check
  async isFirstOpen(): Promise<boolean> {
    try {
      const val = await AsyncStorage.getItem(KEYS.FIRST_OPEN);
      return val !== 'true';
    } catch {
      return true;
    }
  },

  async markFirstOpenDone(): Promise<void> {
    try {
      await AsyncStorage.setItem(KEYS.FIRST_OPEN, 'true');
    } catch (e) {
      console.error('Error saving first open flag', e);
    }
  },

  // Saved User Affirmations
  async getSavedAffirmations(): Promise<Affirmation[]> {
    try {
      const json = await AsyncStorage.getItem(KEYS.AFFIRMATIONS);
      if (!json) {
        // Seed with 2 starter cards when first initialized
        const starterList = [
          STARTER_AFFIRMATION,
          CURATED_AFFIRMATIONS[1],
          CURATED_AFFIRMATIONS[3],
        ];
        await AsyncStorage.setItem(KEYS.AFFIRMATIONS, JSON.stringify(starterList));
        return starterList;
      }
      return JSON.parse(json);
    } catch {
      return [STARTER_AFFIRMATION];
    }
  },

  async addAffirmation(affirmation: Omit<Affirmation, 'id' | 'createdAt'>): Promise<Affirmation> {
    const list = await Storage.getSavedAffirmations();
    const newAffirmation: Affirmation = {
      ...affirmation,
      id: 'user-' + Date.now() + '-' + Math.random().toString(36).substr(2, 4),
      createdAt: new Date().toISOString(),
      isCurated: false,
    };
    const updated = [newAffirmation, ...list];
    await AsyncStorage.setItem(KEYS.AFFIRMATIONS, JSON.stringify(updated));
    return newAffirmation;
  },

  async saveAffirmation(affirmation: Affirmation): Promise<void> {
    const list = await Storage.getSavedAffirmations();
    const index = list.findIndex(item => item.id === affirmation.id);
    if (index >= 0) {
      list[index] = affirmation;
    } else {
      list.unshift(affirmation);
    }
    await AsyncStorage.setItem(KEYS.AFFIRMATIONS, JSON.stringify(list));
  },

  async deleteAffirmation(id: string): Promise<void> {
    const list = await Storage.getSavedAffirmations();
    const filtered = list.filter(item => item.id !== id);
    await AsyncStorage.setItem(KEYS.AFFIRMATIONS, JSON.stringify(filtered));
  },

  async toggleFavorite(id: string): Promise<boolean> {
    const list = await Storage.getSavedAffirmations();
    const index = list.findIndex(item => item.id === id);
    if (index >= 0) {
      list[index].isFavorite = !list[index].isFavorite;
      await AsyncStorage.setItem(KEYS.AFFIRMATIONS, JSON.stringify(list));
      return list[index].isFavorite || false;
    }
    
    // If it's a curated affirmation not yet saved in user list
    const curated = CURATED_AFFIRMATIONS.find(item => item.id === id);
    if (curated) {
      const copy = { ...curated, isFavorite: true };
      list.unshift(copy);
      await AsyncStorage.setItem(KEYS.AFFIRMATIONS, JSON.stringify(list));
      return true;
    }
    return false;
  },

  // Notification Settings
  async getNotificationSettings(): Promise<NotificationSettings> {
    try {
      const json = await AsyncStorage.getItem(KEYS.NOTIFICATIONS);
      return json ? JSON.parse(json) : DEFAULT_NOTIFICATIONS;
    } catch {
      return DEFAULT_NOTIFICATIONS;
    }
  },

  async saveNotificationSettings(settings: NotificationSettings): Promise<void> {
    await AsyncStorage.setItem(KEYS.NOTIFICATIONS, JSON.stringify(settings));
  },

  // Widget Settings
  async getWidgetConfig(): Promise<WidgetConfig> {
    try {
      const json = await AsyncStorage.getItem(KEYS.WIDGET);
      return json ? JSON.parse(json) : DEFAULT_WIDGET;
    } catch {
      return DEFAULT_WIDGET;
    }
  },

  async saveWidgetConfig(config: WidgetConfig): Promise<void> {
    await AsyncStorage.setItem(KEYS.WIDGET, JSON.stringify(config));
  },
};
