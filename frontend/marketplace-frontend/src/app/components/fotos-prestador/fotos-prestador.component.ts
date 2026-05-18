import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FotoService } from '../../services/foto.service';
import { FotoPrestador } from '../../models/models';

@Component({
  selector: 'app-fotos-prestador',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './fotos-prestador.component.html'
})
export class FotosPrestadorComponent implements OnInit {
  @Input() prestadorId!: number;
  @Input() editavel = false;
  @Output() fotosChange = new EventEmitter<number>();

  fotos: FotoPrestador[] = [];
  carregando = false;
  enviando = false;
  erro = '';
  readonly MAX_FOTOS = 10;

  constructor(private fotoService: FotoService) {}

  ngOnInit(): void {
    if (this.prestadorId) this.carregar();
  }

  carregar(): void {
    this.carregando = true;
    this.fotoService.listar(this.prestadorId).subscribe({
      next: (fotos) => { this.fotos = fotos; this.carregando = false; this.fotosChange.emit(fotos.length); },
      error: () => { this.carregando = false; }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    input.value = '';

    const tiposAceitos = ['image/jpeg', 'image/png', 'image/webp'];
    if (!tiposAceitos.includes(file.type)) {
      this.erro = 'Tipo não suportado. Use JPEG, PNG ou WEBP.';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.erro = 'Arquivo muito grande. Máximo 5 MB.';
      return;
    }

    this.erro = '';
    this.enviando = true;
    this.fotoService.upload(file).subscribe({
      next: (foto) => { this.fotos = [...this.fotos, foto]; this.enviando = false; this.fotosChange.emit(this.fotos.length); },
      error: (err) => {
        this.erro = err?.error?.message || 'Erro ao enviar foto. Tente novamente.';
        this.enviando = false;
      }
    });
  }

  excluir(fotoId: number): void {
    this.fotoService.deletar(fotoId).subscribe({
      next: () => { this.fotos = this.fotos.filter(f => f.id !== fotoId); this.fotosChange.emit(this.fotos.length); },
      error: () => { this.erro = 'Erro ao excluir foto.'; }
    });
  }

  urlCompleta(url: string): string {
    return this.fotoService.urlCompleta(url);
  }

  get podeAdicionar(): boolean {
    return this.fotos.length < this.MAX_FOTOS;
  }
}
