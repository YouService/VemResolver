import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

// Importações de UI
import { MatIconModule } from '@angular/material/icon';

// Interfaces e Serviços (Verifique se os caminhos batem com sua estrutura)
import { Categoria } from '../../models/models';
import { CategoriaService } from '../../services/categoria.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    MatIconModule
  ],
  // Garanta que o arquivo home.component.html existe nesta MESMA pasta
  templateUrl: './home.component.html', 
  styles: [`
    :host { display: block; }
    .hide-scrollbar::-webkit-scrollbar { display: none; }
    .hide-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
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
        if (response.content.length > 0 && response.content.length < 10) {
           this.categorias = [...response.content, ...response.content, ...response.content];
        } else {
           this.categorias = response.content;
        }
      },
      error: (error) => console.error('Erro ao carregar categorias:', error)
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
      'Eletricista': '⚡', 'Encanador': '🔧', 'Pintor': '🎨',
      'Pedreiro': '🧱', 'Marceneiro': '🪚', 'Jardineiro': '🌿',
      'Diarista': '🧹', 'Mecânico': '🔩', 'Desenvolvedor': '💻'
    };
    return icones[nome] || '🛠️';
  }

  buscarServico() {
    if (this.searchTerm) {
      this.router.navigate(['/servicos'], { queryParams: { busca: this.searchTerm } });
    }
  }

  selecionarCategoria(categoria: Categoria) {
    this.router.navigate(['/servicos'], { queryParams: { categoria: categoria.id } });
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