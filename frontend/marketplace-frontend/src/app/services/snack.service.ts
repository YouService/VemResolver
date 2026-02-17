import { Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarHorizontalPosition, MatSnackBarVerticalPosition } from '@angular/material/snack-bar';
import { GlobalSnackBarComponent } from '../components/global-snack-bar/global-snack-bar.component';

@Injectable({ providedIn: 'root' })
export class SnackService {
  constructor(private snackBar: MatSnackBar) {}

  open(message: string, title?: string, type?: string, duration = 5000) {
    this.snackBar.openFromComponent(GlobalSnackBarComponent, {
      data: { message, title, type },
      horizontalPosition: 'end' as MatSnackBarHorizontalPosition,
      verticalPosition: 'top' as MatSnackBarVerticalPosition,
      duration,
      panelClass: ['global-snack-bar-panel', type ? `global-snack-${type}` : 'global-snack-info']
    });
  }
}
