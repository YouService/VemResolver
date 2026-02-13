import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cookie-banner',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (!accepted) {
      <div class="fixed bottom-0 left-0 right-0 bg-gray-900 text-white p-4 z-[100] shadow-lg animate-fade-in-up">
        <div class="container mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
          <div class="text-sm md:text-base text-gray-300">
            <p>
              <strong>Este site utiliza cookies</strong> para garantir que você tenha a melhor experiência de navegação. 
              Ao continuar, você concorda com a nossa política de privacidade.
            </p>
          </div>
          <div class="flex gap-4">
            <button (click)="accept()" class="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg font-bold transition whitespace-nowrap">
              Aceitar e Fechar
            </button>
          </div>
        </div>
      </div>
    }
  `,
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