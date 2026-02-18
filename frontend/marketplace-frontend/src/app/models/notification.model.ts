export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface Notification {
  id: string;
  message: string;
  title?: string;
  type: NotificationType;
  duration?: number;
  action?: string;
  onAction?: () => void;
}
