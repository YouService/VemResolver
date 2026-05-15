import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cookie-banner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cookie-banner.component.html',
  styles: [`
    @keyframes fadeInUp {
      from { transform: translateY(100%); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
    .animate-fade-in-up {
      animation: fadeInUp 0.5s ease-out forwards;
    }
  `]
})
export class CookieBannerComponent implements OnInit {
  accepted = true; // Padrão true para evitar flash antes de ler o localStorage

  ngOnInit() {
    // Verifica se já aceitou
    const consent = localStorage.getItem('cookieConsent');
    if (!consent) {
      this.accepted = false;
    }
  }

  accept() {
    localStorage.setItem('cookieConsent', 'true');
    this.accepted = true;
  }
}
