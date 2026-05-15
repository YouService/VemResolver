import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-seguranca',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  templateUrl: './seguranca.component.html'
})
export class SegurancaComponent {}
