export interface Affirmation {
  id: string;
  text: string;
  reference?: string; // Beatitude or verse reference, e.g. "Matthew 5:3"
  tags: string[];
  isCurated?: boolean;
  isFavorite?: boolean;
  createdAt: string;
}

export type AspectRatioType = '9:19.5' | '9:16' | '1:1';

export interface NotificationSettings {
  enabled: boolean;
  frequency: '3h' | '6h' | 'daily' | 'custom';
  source: 'all' | 'favorites' | string[]; // tag names or all/favorites
  quietStart: string; // HH:mm format e.g. "22:00"
  quietEnd: string;   // HH:mm format e.g. "07:00"
}

export interface WidgetConfig {
  size: 'small' | 'medium' | 'large';
  source: 'all' | 'favorites' | string[];
  refreshMinutes: number;
}
