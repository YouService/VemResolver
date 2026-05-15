import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-verify-account',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatButtonModule
  ],
  templateUrl: './verify-account.component.html'
})
export class VerifyAccountComponent implements OnInit {
  loading = true;
  success = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.loading = false;
      this.success = false;
      this.errorMessage = 'Token de verificação não encontrado.';
      return;
    }

    this.http.get(`${environment.apiUrl}/auth/verify?token=${token}`, { responseType: 'text' })
      .subscribe({
        next: () => {
          this.loading = false;
          this.success = true;
        },
        error: (error) => {
          this.loading = false;
          this.success = false;
          // Tenta extrair a mensagem de erro do backend
          this.errorMessage = error.error?.message || error.error || 'O link de verificação é inválido ou expirou.';
          // Se o backend retornar texto puro como erro
          if (typeof error.error === 'string' && error.error.includes('{')) {
             try {
                const parsed = JSON.parse(error.error);
                if (parsed.message) this.errorMessage = parsed.message;
             } catch (e) {}
          }
        }
      });
  }
}
