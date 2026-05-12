import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CategoriaService } from '../services/categoria.service';
import { Categoria } from '../models/models';
import { MatIconModule } from '@angular/material/icon';
import { MaskDirective } from '../directives/mask.directive';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule, MatIconModule, MaskDirective],
  
  styles: [`
    :host {
      display: block;
    }
    .hide-scrollbar::-webkit-scrollbar {
      display: none;
    }
    .hide-scrollbar {
      -ms-overflow-style: none;
      scrollbar-width: none;
    }
  `]
})
export class HomeComponent implements OnInit {
  @ViewChild('categoriesContainer') categoriesContainer!: ElementRef;
  categorias: Categoria[] = [];
  searchTerm: string = '';
  quickRegisterForm: FormGroup;

  constructor(
    private categoriaService: CategoriaService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.quickRegisterForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      celular: ['', [Validators.required, Validators.minLength(11)]]
    });
  }

  ngOnInit() {
    this.carregarCategorias();
  }

  carregarCategorias() {
    this.categoriaService.listarTodas(0, 50).subscribe({
      next: (response) => {
        // Duplicar conteúdo para dar sensação de volume se tiver poucas
        if (response.content.length > 0 && response.content.length < 10) {
           this.categorias = [...response.content, ...response.content, ...response.content];
        } else {
           this.categorias = response.content;
        }
      },
      error: (error) => {
        console.error('Erro ao carregar categorias:', error);
      }
    });
  }

  scrollLeft() {
    if (this.categoriesContainer) {
      this.categoriesContainer.nativeElement.scrollLeft -= 300;
    }
  }

  scrollRight() {
    if (this.categoriesContainer) {
      this.categoriesContainer.nativeElement.scrollLeft += 300;
    }
  }

  getIconeCategoria(nome: string): string {
    const icones: { [key: string]: string } = {
      'Eletricista': '⚡',
      'Encanador': '🔧',
      'Pintor': '🎨',
      'Pedreiro': '🧱',
      'Marceneiro': '🪚',
      'Jardineiro': '🌿',
      'Diarista': '🧹',
      'Professor Particular': '📚',
      'Mecânico': '🔩',
      'Desenvolvedor': '💻'
    };
    return icones[nome] || '🛠️';
  }

  buscarServico() {
    console.log('Buscando:', this.searchTerm);
  }

  selecionarCategoria(categoria: Categoria) {
    console.log('Categoria selecionada:', categoria);
  }

  iniciarCadastro() {
    if (this.quickRegisterForm.valid) {
      this.router.navigate(['/register'], {
        queryParams: {
          tipo: 'PRESTADOR',
          email: this.quickRegisterForm.value.email,
          telefone: this.quickRegisterForm.value.celular
        }
      });
    } else {
      this.quickRegisterForm.markAllAsTouched();
    }
  }
}