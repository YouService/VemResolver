import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PrestadorService } from '../../services/prestador.service';
import { CategoriaService } from '../../services/categoria.service';
import { Prestador, Categoria } from '../../models/models';

@Component({
  selector: 'app-busca-prestadores',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './busca-prestadores.component.html'
})
export class BuscaPrestadoresComponent implements OnInit {
  categorias: Categoria[] = [];
  prestadores: Prestador[] = [];

  categoriaSelecionada?: number;
  raioKm = 50;
  raiosDisponiveis = [10, 25, 50, 100];

  latUsuario?: number;
  lngUsuario?: number;
  obtendoLocalizacao = false;
  erroLocalizacao = '';
  localizacaoObtida = false;

  loading = false;
  buscaRealizada = false;
  errorMessage = '';

  paginaAtual = 0;
  totalPaginas = 0;
  itensPorPagina = 10;

  constructor(
    private prestadorService: PrestadorService,
    private categoriaService: CategoriaService
  ) {}

  ngOnInit(): void {
    this.categoriaService.listarTodas(0, 100).subscribe({
      next: res => { this.categorias = res.content; },
      error: () => {}
    });
  }

  obterLocalizacao(): void {
    if (!navigator.geolocation) {
      this.erroLocalizacao = 'Geolocalização não suportada pelo navegador.';
      return;
    }
    this.obtendoLocalizacao = true;
    this.erroLocalizacao = '';
    navigator.geolocation.getCurrentPosition(
      pos => {
        this.latUsuario = pos.coords.latitude;
        this.lngUsuario = pos.coords.longitude;
        this.localizacaoObtida = true;
        this.obtendoLocalizacao = false;
        this.buscar(0);
      },
      () => {
        this.erroLocalizacao = 'Não foi possível obter localização. Verifique as permissões do navegador.';
        this.obtendoLocalizacao = false;
      }
    );
  }

  buscar(pagina: number): void {
    if (!this.latUsuario || !this.lngUsuario) return;
    this.loading = true;
    this.errorMessage = '';
    this.prestadorService.buscarProximos(
      this.latUsuario,
      this.lngUsuario,
      this.raioKm,
      this.categoriaSelecionada,
      pagina,
      this.itensPorPagina
    ).subscribe({
      next: res => {
        this.prestadores = res.content;
        this.totalPaginas = res.totalPages;
        this.paginaAtual = pagina;
        this.buscaRealizada = true;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Erro ao buscar prestadores. Tente novamente.';
        this.loading = false;
      }
    });
  }

  irParaPagina(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPaginas) {
      this.buscar(pagina);
    }
  }

  atualizarBusca(): void {
    if (this.localizacaoObtida) {
      this.buscar(0);
    }
  }

  getEstrelas(media: number): number[] {
    return [1, 2, 3, 4, 5];
  }
}
