import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Notification } from '../../models/notification.model';
import { NotificationService } from '../../services/notification.service';
import { NotificationComponent } from '../notification/notification.component';

@Component({
  selector: 'app-notification-container',
  standalone: true,
  imports: [CommonModule, NotificationComponent],
  template: `
    <div class="fixed top-4 right-4 z-[9999] pointer-events-none">
      <div class="flex flex-col gap-3 pointer-events-auto">
        @for (notification of notifications$ | async; track notification.id) {
          <app-notification
            [notification]="notification"
            (close)="onNotificationClose($event)"
            (action)="onNotificationAction($event)"
          />
        }
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
    }
  `]
})
export class NotificationContainerComponent implements OnInit {
  notifications$: Observable<Notification[]>;

  constructor(private notificationService: NotificationService) {
    this.notifications$ = this.notificationService.getNotifications();
  }

  ngOnInit(): void {
    // Component initialized
  }

  onNotificationClose(id: string): void {
    this.notificationService.remove(id);
  }

  onNotificationAction(notification: Notification | void): void {
    // Action is handled by the notification service
    if (notification) {
      console.log('Notification action triggered:', notification);
    }
  }
}
