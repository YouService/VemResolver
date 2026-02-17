import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MAT_SNACK_BAR_DATA } from '@angular/material/snack-bar';

@Component({
  selector: 'app-global-snack-bar',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  templateUrl: './global-snack-bar.component.html',
  styleUrls: ['./global-snack-bar.component.scss']
})
export class GlobalSnackBarComponent {
  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: { title?: string; message: string; type?: string }) {}
}
