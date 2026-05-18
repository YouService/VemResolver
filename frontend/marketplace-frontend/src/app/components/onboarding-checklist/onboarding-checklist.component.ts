import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PrestadorService } from '../../services/prestador.service';
import { CompletudePrestador, ItemChecklist } from '../../models/models';

@Component({
  selector: 'app-onboarding-checklist',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './onboarding-checklist.component.html'
})
export class OnboardingChecklistComponent implements OnInit {
  completude?: CompletudePrestador;
  loading = false;
  recolhido = false;

  constructor(
    private prestadorService: PrestadorService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.loading = true;
    this.prestadorService.getCompletude().subscribe({
      next: (c) => {
        this.completude = c;
        this.recolhido = c.percentual === 100;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  get corBarra(): string {
    if (!this.completude) return 'bg-gray-400';
    if (this.completude.percentual < 50) return 'bg-red-500';
    if (this.completude.percentual < 80) return 'bg-yellow-500';
    return 'bg-green-500';
  }

  get textoCor(): string {
    if (!this.completude) return 'text-gray-600';
    if (this.completude.percentual < 50) return 'text-red-600';
    if (this.completude.percentual < 80) return 'text-yellow-600';
    return 'text-green-600';
  }

  get completo(): boolean {
    return (this.completude?.percentual ?? 0) === 100;
  }

  navegarItem(item: ItemChecklist): void {
    if (!item.concluido && item.linkDestino) {
      this.router.navigate([item.linkDestino]);
    }
  }

  toggleRecolhido(): void {
    this.recolhido = !this.recolhido;
  }
}
