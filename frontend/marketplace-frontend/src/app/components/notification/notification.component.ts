import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Notification, NotificationType } from '../../models/notification.model';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule],
  template: `
    <div
      class="notification-wrapper w-full max-w-md animate-slide-in"
      [@.disabled]="true"
    >
      <div
        class="rounded-lg shadow-lg border-l-4 p-4 flex items-start gap-4 backdrop-blur-sm"
        [ngClass]="getNotificationClasses()"
      >
        <!-- Ícone -->
        <div class="flex-shrink-0 flex items-center justify-center w-6 h-6 mt-0.5">
          <mat-icon class="text-lg" [ngClass]="getIconColor()">
            {{ getIcon() }}
          </mat-icon>
        </div>

        <!-- Conteúdo -->
        <div class="flex-1 min-w-0">
          <div *ngIf="notification.title" class="font-semibold mb-1" [ngClass]="getTitleColor()">
            {{ notification.title }}
          </div>
          <p class="text-sm break-words" [ngClass]="getTextColor()">
            {{ notification.message }}
          </p>

          <!-- Botão de ação (opcional) -->
          <div *ngIf="notification.action" class="mt-2">
            <button
              (click)="onActionClick()"
              class="text-xs font-semibold px-3 py-1 rounded transition-all hover:opacity-80"
              [ngClass]="getActionButtonColor()"
            >
              {{ notification.action }}
            </button>
          </div>
        </div>

        <!-- Botão de fechar -->
        <button
          (click)="onClose()"
          mat-icon-button
          class="flex-shrink-0 h-6 w-6 p-0 hover:opacity-70 transition-opacity"
          [ngClass]="getCloseButtonColor()"
          aria-label="Fechar notificação"
        >
          <mat-icon class="text-lg">close</mat-icon>
        </button>
      </div>

      <!-- Barra de progresso para auto-dismissal -->
      <div
        *ngIf="notification.duration && notification.duration > 0"
        class="h-1 mt-2 rounded-b-lg overflow-hidden"
        [ngClass]="getProgressBarBackground()"
      >
        <div
          class="h-full rounded-b-lg transition-all"
          [ngClass]="getProgressBarColor()"
          [style.animation]="'progress ' + (notification.duration / 1000) + 's linear forwards'"
        ></div>
      </div>
    </div>

    <style>
      @keyframes slideIn {
        from {
          transform: translateX(400px);
          opacity: 0;
        }
        to {
          transform: translateX(0);
          opacity: 1;
        }
      }

      @keyframes slideOut {
        from {
          transform: translateX(0);
          opacity: 1;
        }
        to {
          transform: translateX(400px);
          opacity: 0;
        }
      }

      @keyframes progress {
        from {
          width: 100%;
        }
        to {
          width: 0%;
        }
      }

      .animate-slide-in {
        animation: slideIn 0.3s ease-out forwards;
      }

      .animate-slide-out {
        animation: slideOut 0.3s ease-in forwards;
      }
    </style>
  `,
  styles: [`
    .notification-wrapper {
      margin-bottom: 12px;
    }
  `]
})
export class NotificationComponent implements OnInit, OnDestroy {
  @Input() notification!: Notification;
  @Output() close = new EventEmitter<string>();
  @Output() action = new EventEmitter<Notification>();

  private isClosing = false;

  ngOnInit() {
    // Auto-close animation if duration is 0 (stay open indefinitely)
    // Will be removed by the container when close is clicked
  }

  ngOnDestroy() {
    // Cleanup
  }

  getIcon(): string {
    const iconMap: Record<NotificationType, string> = {
      success: 'check_circle',
      error: 'error',
      warning: 'warning',
      info: 'info'
    };
    return iconMap[this.notification.type];
  }

  getIconColor(): string {
    const colorMap: Record<NotificationType, string> = {
      success: 'text-emerald-600',
      error: 'text-red-600',
      warning: 'text-amber-600',
      info: 'text-blue-600'
    };
    return colorMap[this.notification.type];
  }

  getNotificationClasses(): string {
    const baseClasses = 'relative overflow-hidden';

    const styleMap: Record<NotificationType, string> = {
      success: `${baseClasses} bg-emerald-50 border-emerald-400 text-emerald-900`,
      error: `${baseClasses} bg-red-50 border-red-400 text-red-900`,
      warning: `${baseClasses} bg-amber-50 border-amber-400 text-amber-900`,
      info: `${baseClasses} bg-blue-50 border-blue-400 text-blue-900`
    };
    return styleMap[this.notification.type];
  }

  getTitleColor(): string {
    const colorMap: Record<NotificationType, string> = {
      success: 'text-emerald-800',
      error: 'text-red-800',
      warning: 'text-amber-800',
      info: 'text-blue-800'
    };
    return colorMap[this.notification.type];
  }

  getTextColor(): string {
    const colorMap: Record<NotificationType, string> = {
      success: 'text-emerald-700',
      error: 'text-red-700',
      warning: 'text-amber-700',
      info: 'text-blue-700'
    };
    return colorMap[this.notification.type];
  }

  getCloseButtonColor(): string {
    const colorMap: Record<NotificationType, string> = {
      success: 'text-emerald-600 hover:bg-emerald-100',
      error: 'text-red-600 hover:bg-red-100',
      warning: 'text-amber-600 hover:bg-amber-100',
      info: 'text-blue-600 hover:bg-blue-100'
    };
    return colorMap[this.notification.type];
  }

  getActionButtonColor(): string {
    const colorMap: Record<NotificationType, string> = {
      success: 'bg-emerald-600 hover:bg-emerald-700 text-white',
      error: 'bg-red-600 hover:bg-red-700 text-white',
      warning: 'bg-amber-600 hover:bg-amber-700 text-white',
      info: 'bg-blue-600 hover:bg-blue-700 text-white'
    };
    return colorMap[this.notification.type];
  }

  getProgressBarBackground(): string {
    const colorMap: Record<NotificationType, string> = {
      success: 'bg-emerald-200',
      error: 'bg-red-200',
      warning: 'bg-amber-200',
      info: 'bg-blue-200'
    };
    return colorMap[this.notification.type];
  }

  getProgressBarColor(): string {
    const colorMap: Record<NotificationType, string> = {
      success: 'bg-emerald-600',
      error: 'bg-red-600',
      warning: 'bg-amber-600',
      info: 'bg-blue-600'
    };
    return colorMap[this.notification.type];
  }

  onClose(): void {
    if (this.isClosing) return;
    this.isClosing = true;
    this.close.emit(this.notification.id);
  }

  onActionClick(): void {
    if (this.notification.onAction) {
      this.notification.onAction();
    }
    this.action.emit(this.notification);
  }
}
