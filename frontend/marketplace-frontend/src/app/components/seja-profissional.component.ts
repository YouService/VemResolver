import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-seja-profissional',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  templateUrl: './seja-profissional.component.html'
})
export class SejaProfissionalComponent {}
