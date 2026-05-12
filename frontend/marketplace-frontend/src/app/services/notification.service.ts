import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Notification, NotificationType } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notifications$ = new BehaviorSubject<Notification[]>([]);
  private notificationIdCounter = 0;

  getNotifications(): Observable<Notification[]> {
    return this.notifications$.asObservable();
  }

  show(
    message: string,
    type: NotificationType = 'info',
    duration: number = 5000,
    title?: string,
    action?: string,
    onAction?: () => void
  ): string {
    const id = `notification-${++this.notificationIdCounter}`;

    const notification: Notification = {
      id,
      message,
      type,
      duration,
      title,
      action,
      onAction
    };

    const currentNotifications = this.notifications$.value;
    this.notifications$.next([...currentNotifications, notification]);

    // Auto remove após duration (se duration > 0)
    if (duration > 0) {
      setTimeout(() => this.remove(id), duration);
    }

    return id;
  }

  success(message: string, title?: string, duration: number = 5000, action?: string, onAction?: () => void): string {
    return this.show(message, 'success', duration, title || 'Sucesso!', action, onAction);
  }

  error(message: string, title?: string, duration: number = 5000, action?: string, onAction?: () => void): string {
    return this.show(message, 'error', duration, title || 'Erro!', action, onAction);
  }

  warning(message: string, title?: string, duration: number = 5000, action?: string, onAction?: () => void): string {
    return this.show(message, 'warning', duration, title || 'Atenção!', action, onAction);
  }

  info(message: string, title?: string, duration: number = 5000, action?: string, onAction?: () => void): string {
    return this.show(message, 'info', duration, title || 'Informação', action, onAction);
  }

  remove(id: string): void {
    const currentNotifications = this.notifications$.value;
    this.notifications$.next(
      currentNotifications.filter(notification => notification.id !== id)
    );
  }

  removeAll(): void {
    this.notifications$.next([]);
  }

  removeByType(type: NotificationType): void {
    const currentNotifications = this.notifications$.value;
    this.notifications$.next(
      currentNotifications.filter(notification => notification.type !== type)
    );
  }
}
