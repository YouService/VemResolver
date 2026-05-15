import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SolicitacaoService } from '../services/solicitacao.service';
import { CategoriaService } from '../services/categoria.service';
import { Categoria, SolicitacaoServico } from '../models/models';

@Component({
  selector: 'app-cliente-nova-solicitacao',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cliente-nova-solicitacao.component.html'
})
export class ClienteNovaSolicitacaoComponent implements OnInit {
  solicitacaoForm: FormGroup;
  categorias: Categoria[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private solicitacaoService: SolicitacaoService,
    private categoriaService: CategoriaService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.solicitacaoForm = this.fb.group({
      categoriaId: ['', Validators.required],
      descricao: ['', [Validators.required, Validators.minLength(20)]],
      cidade: ['', Validators.required],
      estado: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.carregarCategorias();
  }

  carregarCategorias() {
    const categoriaIdParam = this.route.snapshot.queryParamMap.get('categoriaId');
    this.categoriaService.listarTodas(0, 100).subscribe({
      next: (response) => {
        this.categorias = response.content;
        if (categoriaIdParam) {
          this.solicitacaoForm.patchValue({ categoriaId: categoriaIdParam });
        }
      },
      error: () => {
        this.errorMessage = 'Erro ao carregar categorias';
      }
    });
  }

  enviarSolicitacao() {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.solicitacaoForm.invalid) {
      Object.keys(this.solicitacaoForm.controls).forEach(key => {
        this.solicitacaoForm.get(key)?.markAsTouched();
      });
      this.errorMessage = 'Por favor, preencha todos os campos obrigatórios';
      return;
    }

    this.loading = true;

    const solicitacao: SolicitacaoServico = {
      categoriaId: parseInt(this.solicitacaoForm.value.categoriaId),
      descricao: this.solicitacaoForm.value.descricao,
      cidade: this.solicitacaoForm.value.cidade,
      estado: this.solicitacaoForm.value.estado
    };

    this.solicitacaoService.criar(solicitacao).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Solicitação publicada com sucesso! Redirecionando...';
        setTimeout(() => {
          this.router.navigate(['/cliente/dashboard']);
        }, 2000);
      },
      error: (error: any) => {
        this.loading = false;
        console.error('Erro ao criar solicitação:', error);
        this.errorMessage = 'Erro ao publicar solicitação. Tente novamente.';
      }
    });
  }

  voltar() {
    this.router.navigate(['/cliente/dashboard']);
  }
}
