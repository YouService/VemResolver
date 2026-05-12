import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PrestadorService } from '../../services/prestador.service';
import { CategoriaService } from '../../services/categoria.service';
import { AuthService } from '../../services/auth.service';
import { Prestador, Categoria } from '../../models/models';

@Component({
  selector: 'app-prestador-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './prestador-perfil.component.html',
  styleUrls: ['./prestador-perfil.component.css']
})
export class PrestadorPerfilComponent implements OnInit {
  perfilForm: FormGroup;
  categorias: Categoria[] = [];
  categoriasSelecionadas: number[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';
  categoriasInvalidas = false;
  prestadorExistente?: Prestador;

  constructor(
    private fb: FormBuilder,
    private prestadorService: PrestadorService,
    private categoriaService: CategoriaService,
    private authService: AuthService,
    private router: Router
  ) {
    this.perfilForm = this.fb.group({
      descricaoPerfil: ['', [Validators.required, Validators.minLength(50)]],
      cidade: ['', Validators.required],
      estado: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.carregarCategorias();
    this.carregarPerfilExistente();
  }

  carregarCategorias() {
    this.categoriaService.listarTodas(0, 100).subscribe({
      next: (response) => {
        this.categorias = response.content;
      },
      error: (error) => {
        console.error('Erro ao carregar categorias:', error);
        this.errorMessage = 'Erro ao carregar categorias';
      }
    });
  }

  carregarPerfilExistente() {
    this.prestadorService.buscarMeuPerfil().subscribe({
      next: (prestador: Prestador) => {
        this.prestadorExistente = prestador;
        this.perfilForm.patchValue({
          descricaoPerfil: prestador.descricaoPerfil || '',
          cidade: prestador.cidade || '',
          estado: prestador.estado || ''
        });
        this.categoriasSelecionadas = prestador.categoriasIds || [];
      },
      error: () => {
        // 404 esperado para prestador sem perfil ainda — formulário fica em branco para criação
      }
    });
  }

  isCategoriaSelected(categoriaId: number): boolean {
    return this.categoriasSelecionadas.includes(categoriaId);
  }

  onCategoriaChange(event: any, categoriaId: number) {
    if (event.target.checked) {
      this.categoriasSelecionadas.push(categoriaId);
    } else {
      const index = this.categoriasSelecionadas.indexOf(categoriaId);
      if (index > -1) {
        this.categoriasSelecionadas.splice(index, 1);
      }
    }
    this.categoriasInvalidas = false;
  }

  calcularCompletude(): number {
    let pontos = 0;
    const total = 4;

    if (this.perfilForm.get('descricaoPerfil')?.valid) pontos++;
    if (this.perfilForm.get('cidade')?.valid) pontos++;
    if (this.perfilForm.get('estado')?.valid) pontos++;
    if (this.categoriasSelecionadas.length > 0) pontos++;

    return Math.round((pontos / total) * 100);
  }

  salvarPerfil() {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.perfilForm.invalid) {
      Object.keys(this.perfilForm.controls).forEach(key => {
        this.perfilForm.get(key)?.markAsTouched();
      });
      this.errorMessage = 'Por favor, preencha todos os campos obrigatórios';
      return;
    }

    if (this.categoriasSelecionadas.length === 0) {
      this.categoriasInvalidas = true;
      this.errorMessage = 'Selecione pelo menos uma categoria de serviço';
      return;
    }

    this.loading = true;
    const userId = this.authService.getCurrentUserId();

    const prestadorData: Prestador = {
      id: this.prestadorExistente?.id || 0,
      usuarioId: userId!,
      categoriasIds: this.categoriasSelecionadas,
      descricaoPerfil: this.perfilForm.value.descricaoPerfil,
      cidade: this.perfilForm.value.cidade,
      estado: this.perfilForm.value.estado
    };

    const request = this.prestadorExistente
      ? this.prestadorService.atualizar(this.prestadorExistente.id, prestadorData)
      : this.prestadorService.criar(prestadorData);

    request.subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Perfil salvo com sucesso!';
        setTimeout(() => {
          this.router.navigate(['/prestador/dashboard']);
        }, 2000);
      },
      error: (error: any) => {
        this.loading = false;
        console.error('Erro ao salvar perfil:', error);
        this.errorMessage = 'Erro ao salvar perfil. Tente novamente.';
      }
    });
  }

  voltarDashboard() {
    this.router.navigate(['/prestador/dashboard']);
  }
}
