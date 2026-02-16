import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export type AlertType = 'success' | 'error' | 'warning' | 'info';

@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    @if (message) {
      <div [ngClass]="getAlertClasses()" role="alert">
        <div class="flex items-start">
          <mat-icon [ngClass]="getIconClasses()" class="mr-3 flex-shrink-0">
            {{ getIcon() }}
          </mat-icon>
          <div class="flex-1">
            @if (title) {
              <h3 [ngClass]="getTitleClasses()">{{ title }}</h3>
            }
            <p [ngClass]="getTextClasses()">{{ message }}</p>
          </div>
        </div>
      </div>
    }
  `,
  styles: []
})
export class AlertComponent {
  @Input() message: string = '';
  @Input() title: string = '';
  @Input() type: AlertType = 'info';

  getAlertClasses(): string {
    const baseClasses = 'rounded-lg p-4 mb-4';
    const typeClasses = {
      success: 'bg-green-50 border border-green-200',
      error: 'bg-red-50 border border-red-200',
      warning: 'bg-yellow-50 border border-yellow-200',
      info: 'bg-blue-50 border border-blue-200'
    };
    return `${baseClasses} ${typeClasses[this.type]}`;
  }

  getTextClasses(): string {
    const typeClasses = {
      success: 'text-green-700',
      error: 'text-red-700',
      warning: 'text-yellow-700',
      info: 'text-blue-700'
    };
    return typeClasses[this.type];
  }

  getTitleClasses(): string {
    const baseClasses = 'font-semibold mb-1';
    const typeClasses = {
      success: 'text-green-800',
      error: 'text-red-800',
      warning: 'text-yellow-800',
      info: 'text-blue-800'
    };
    return `${baseClasses} ${typeClasses[this.type]}`;
  }

  getIconClasses(): string {
    const typeClasses = {
      success: 'text-green-600',
      error: 'text-red-600',
      warning: 'text-yellow-600',
      info: 'text-blue-600'
    };
    return typeClasses[this.type];
  }

  getIcon(): string {
    const iconMap = {
      success: 'check_circle',
      error: 'error',
      warning: 'warning',
      info: 'info'
    };
    return iconMap[this.type];
  }
}
